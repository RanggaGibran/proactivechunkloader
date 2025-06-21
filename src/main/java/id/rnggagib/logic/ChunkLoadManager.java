package id.rnggagib.logic;

import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.block.BlockFace;
import org.bukkit.util.Vector;
import org.bukkit.Location;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Queue;
import java.util.PriorityQueue;
import java.util.Comparator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.stream.Collectors;

/**
 * Manages the logic for queuing and loading chunks proactively
 * Enhanced version with priority-based loading and performance monitoring
 */
public class ChunkLoadManager {
    private final JavaPlugin plugin;
    private final Logger logger;
    private final Queue<PrioritizedChunk> chunkQueue;
    private final Set<ChunkCoordinate> queuedChunks;
    private BukkitTask loaderTask;
    private BukkitTask statsTask;
    private BukkitTask movementTrackingTask;
    
    // Configuration values
    private int maxChunksPerTick;
    private int frontierMinDistance;
    private int frontierMaxDistance;
    private int baseChunkWidth;
    private boolean debug;
    private boolean adaptiveTpsScaling;
    private double minTps;
    private boolean adaptiveConePrediction;
    private int playerHistorySize;
    private double speedInfluenceFactor;
    private boolean enableExtraDetailedLogging;
    
    // Performance stats
    private final Map<Long, Integer> chunksLoadedHistory = new HashMap<>();
    private final AtomicInteger totalChunksLoaded = new AtomicInteger(0);
    private final AtomicInteger chunksLoadedThisMinute = new AtomicInteger(0);
    private long lastStatsReset = System.currentTimeMillis();
    private final Map<String, Long> chunkLoadTimes = new ConcurrentHashMap<>();
    private final LinkedHashMap<Integer, Integer> priorityDistribution = new LinkedHashMap<>();
    
    // Store the last processed chunk for each player to prevent redundant processing
    private final Map<Player, ChunkCoordinate> lastPlayerChunks = new HashMap<>();
    
    // Player movement history for better prediction
    private final Map<Player, List<PlayerMovement>> playerMovementHistory = new HashMap<>();
    
    public ChunkLoadManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        // Initialize queue with priority comparator (higher values first, then by timestamp)
        this.chunkQueue = new PriorityQueue<>(
            Comparator.comparing(PrioritizedChunk::getPriority).reversed()
                .thenComparing(PrioritizedChunk::getTimestamp)
        );
        this.queuedChunks = new HashSet<>();
        
        // Load configuration
        loadConfig();
    }
    
    /**
     * Load configuration values from config.yml
     */
    public void loadConfig() {
        plugin.saveDefaultConfig();
        
        maxChunksPerTick = plugin.getConfig().getInt("max-chunks-per-tick", 1);
        frontierMinDistance = plugin.getConfig().getInt("frontier-distance.min", 1);
        frontierMaxDistance = plugin.getConfig().getInt("frontier-distance.max", 3);
        baseChunkWidth = plugin.getConfig().getInt("frontier-width", 1);
        debug = plugin.getConfig().getBoolean("debug", false);
        
        // Advanced configuration
        adaptiveTpsScaling = plugin.getConfig().getBoolean("performance.adaptive-tps-scaling", true);
        minTps = plugin.getConfig().getDouble("performance.minimum-tps", 18.0);
        adaptiveConePrediction = plugin.getConfig().getBoolean("advanced.adaptive-cone-prediction", true);
        playerHistorySize = plugin.getConfig().getInt("advanced.player-history-size", 10);
        speedInfluenceFactor = plugin.getConfig().getDouble("advanced.speed-influence-factor", 1.0);
        enableExtraDetailedLogging = plugin.getConfig().getBoolean("advanced.extra-detailed-logging", false);
        
        if (debug) {
            logger.info("Config loaded: maxChunksPerTick=" + maxChunksPerTick + 
                       ", frontierDistance=" + frontierMinDistance + "-" + frontierMaxDistance +
                       ", baseChunkWidth=" + baseChunkWidth);
        }
    }
    
    /**
     * Start the chunk loading task
     */
    public void startTask() {
        // Cancel any existing tasks
        stopTask();
        
        // Chunk loading task
        this.loaderTask = new BukkitRunnable() {
            @Override
            public void run() {
                // Adjust maxChunksPerTick based on server TPS if adaptive scaling is enabled
                int chunksToProcess = maxChunksPerTick;
                if (adaptiveTpsScaling) {
                    double tps = plugin.getServer().getTPS()[0]; // Get current 1-minute TPS
                    if (tps < minTps) {
                        // Reduce chunks processed when TPS is low
                        double ratio = Math.max(0.1, (tps / 20.0));
                        chunksToProcess = Math.max(1, (int)(maxChunksPerTick * ratio));
                        if (debug) {
                            logger.info("TPS low (" + String.format("%.2f", tps) + "), reduced chunk processing to " + chunksToProcess);
                        }
                    }
                }
                
                processNextChunk(chunksToProcess);
            }
        }.runTaskTimerAsynchronously(plugin, 1L, 5L); // Run every 5 ticks (1/4 second)
        
        // Stats tracking task
        this.statsTask = new BukkitRunnable() {
            @Override
            public void run() {
                // Record stats every minute
                long now = System.currentTimeMillis();
                long minute = now / 60000; // Current minute
                
                // Store chunks loaded this minute
                int loaded = chunksLoadedThisMinute.getAndSet(0);
                chunksLoadedHistory.put(minute, loaded);
                
                // Clean up old history (keep last hour only)
                chunksLoadedHistory.entrySet().removeIf(entry -> entry.getKey() < (minute - 60));
                
                // Reset counter
                lastStatsReset = now;
                
                if (debug) {
                    logger.info("Stats: Loaded " + loaded + " chunks in the last minute. Total: " + totalChunksLoaded.get());
                    
                    // Log priority distribution
                    if (enableExtraDetailedLogging && !priorityDistribution.isEmpty()) {
                        StringBuilder sb = new StringBuilder("Priority distribution: ");
                        priorityDistribution.forEach((priority, count) -> {
                            sb.append(priority).append("=").append(count).append(", ");
                        });
                        logger.info(sb.substring(0, Math.max(sb.length() - 2, 0)));
                        priorityDistribution.clear();
                    }
                    
                    // Log average chunk load time
                    if (!chunkLoadTimes.isEmpty()) {
                        double avgLoadTime = chunkLoadTimes.values().stream()
                                .mapToLong(Long::longValue)
                                .average()
                                .orElse(0);
                        logger.info("Average chunk load time: " + String.format("%.2f", avgLoadTime) + "ms");
                        chunkLoadTimes.clear();
                    }
                }
            }
        }.runTaskTimerAsynchronously(plugin, 1200L, 1200L); // Run every minute (1200 ticks)
        
        // Movement tracking task for velocity prediction
        this.movementTrackingTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (adaptiveConePrediction) {
                    for (Player player : plugin.getServer().getOnlinePlayers()) {
                        trackPlayerMovement(player);
                    }
                }
            }
        }.runTaskTimer(plugin, 5L, 5L); // Run every 5 ticks
        
        logger.info("ProactiveChunkLoader tasks started");
    }

    /**
     * Stop the chunk loading task
     */
    public void stopTask() {
        if (loaderTask != null && !loaderTask.isCancelled()) {
            loaderTask.cancel();
            logger.info("ProactiveChunkLoader loading task stopped");
        }
        if (statsTask != null && !statsTask.isCancelled()) {
            statsTask.cancel();
            logger.info("ProactiveChunkLoader stats task stopped");
        }
        if (movementTrackingTask != null && !movementTrackingTask.isCancelled()) {
            movementTrackingTask.cancel();
            logger.info("ProactiveChunkLoader movement tracking task stopped");
        }
    }
    
    /**
     * Track player movement for velocity prediction
     * @param player The player to track
     */
    private void trackPlayerMovement(Player player) {
        List<PlayerMovement> history = playerMovementHistory.computeIfAbsent(player, k -> new ArrayList<>());
        
        // Add current position to history
        Location loc = player.getLocation();
        PlayerMovement movement = new PlayerMovement(
            loc.getWorld(),
            loc.getX(),
            loc.getY(),
            loc.getZ(),
            player.getVelocity(),
            System.currentTimeMillis()
        );
        
        history.add(movement);
        
        // Trim history to configured size
        while (history.size() > playerHistorySize) {
            history.remove(0);
        }
    }
    
    /**
     * Process the next chunk in the queue
     * @param chunksToProcess Number of chunks to process in this tick
     */
    private void processNextChunk(int chunksToProcess) {
        // Process up to chunksToProcess chunks per tick
        for (int i = 0; i < chunksToProcess && !chunkQueue.isEmpty(); i++) {
            PrioritizedChunk prioritizedChunk = chunkQueue.poll();
            if (prioritizedChunk == null) {
                return;
            }
            
            ChunkCoordinate coordinate = prioritizedChunk.getCoordinate();
    
            // Remove from tracking set
            synchronized (queuedChunks) {
                queuedChunks.remove(coordinate);
            }
    
            World world = coordinate.getWorld();
            if (world != null) {
                final int x = coordinate.getX();
                final int z = coordinate.getZ();
                final long startTime = System.currentTimeMillis();
                
                // Update priority distribution stats
                if (enableExtraDetailedLogging) {
                    int priority = prioritizedChunk.getPriority();
                    priorityDistribution.merge(priority, 1, Integer::sum);
                }
                
                // Use native Paper async chunk loading
                world.getChunkAtAsync(x, z).thenAccept(chunk -> {
                    // Update statistics
                    totalChunksLoaded.incrementAndGet();
                    chunksLoadedThisMinute.incrementAndGet();
                    
                    // Record load time
                    long loadTime = System.currentTimeMillis() - startTime;
                    chunkLoadTimes.put(x + "," + z, loadTime);
                    
                    if (plugin.isEnabled() && debug) {
                        logger.info("Preloaded chunk at " + x + "," + z + " in " + world.getName() + 
                                    " (priority: " + prioritizedChunk.getPriority() + ", time: " + loadTime + "ms)");
                    }
                }).exceptionally(ex -> {
                    if (plugin.isEnabled()) {
                        logger.warning("Failed to load chunk at " + x + "," + z + ": " + ex.getMessage());
                    }
                    return null;
                });
            }
        }
    }

    /**
     * Queue chunks for loading based on player's position, velocity, and facing direction
     * @param player The player to queue chunks for
     */
    public void queueChunksForPlayer(Player player) {
        World world = player.getWorld();
        Chunk currentChunk = player.getLocation().getChunk();
        ChunkCoordinate currentCoord = new ChunkCoordinate(world, currentChunk.getX(), currentChunk.getZ());
        
        // Check if player has moved to a new chunk
        ChunkCoordinate lastChunk = lastPlayerChunks.get(player);
        if (lastChunk != null && lastChunk.equals(currentCoord)) {
            return;
        }
        
        // Update last chunk
        lastPlayerChunks.put(player, currentCoord);
        
        // Get server's view distance
        int viewDistance = plugin.getServer().getViewDistance();
        
        // Base position
        int baseX = currentChunk.getX();
        int baseZ = currentChunk.getZ();
        
        // Determine direction and cone properties based on configuration
        ConeProperties cone = calculateConeProperties(player);
        
        // Log calculated cone properties if in debug mode
        if (debug && enableExtraDetailedLogging) {
            logger.info(String.format("Player %s: Cone direction (%.2f, %.2f), width: %d, maxDistance: %d",
                player.getName(), cone.directionX, cone.directionZ, cone.width, cone.maxDistance));
        }
        
        // Queue chunks in the predicted direction using cone pattern
        for (int distance = viewDistance + frontierMinDistance; distance <= viewDistance + cone.maxDistance; distance++) {
            int distancePriority = frontierMaxDistance + 1 - (distance - viewDistance - frontierMinDistance);
            
            // Center position in the direction of movement
            int centerX = baseX + (int)(cone.directionX * distance);
            int centerZ = baseZ + (int)(cone.directionZ * distance);
            
            // Calculate cone width at this distance (wider as it goes further)
            int actualWidth = cone.width + (distance - viewDistance - frontierMinDistance) / 2;
            
            // Load chunks in a cone pattern
            for (int dx = -actualWidth; dx <= actualWidth; dx++) {
                for (int dz = -actualWidth; dz <= actualWidth; dz++) {
                    // Skip chunks outside the cone shape
                    if (!isInCone(dx, dz, cone.directionX, cone.directionZ, actualWidth)) {
                        continue;
                    }
                    
                    int chunkX = centerX + dx;
                    int chunkZ = centerZ + dz;
                    
                    // Calculate priority based on position in cone and distance
                    int centerOffset = Math.abs(dx) + Math.abs(dz);
                    int priority = calculatePriority(distancePriority, centerOffset, cone.speed);
                    
                    // Add to queue with calculated priority
                    addToQueue(world, chunkX, chunkZ, priority);
                }
            }
        }
    }
    
    /**
     * Check if a point (dx, dz) is within the cone defined by direction vector
     * @param dx X offset from center
     * @param dz Z offset from center
     * @param dirX X direction component
     * @param dirZ Z direction component
     * @param width Cone width
     * @return true if the point is in the cone
     */
    private boolean isInCone(int dx, int dz, double dirX, double dirZ, int width) {
        if (dx == 0 && dz == 0) {
            return true; // Center is always in cone
        }
        
        // Calculate angle between (dx,dz) and (dirX,dirZ)
        double dotProduct = dx * dirX + dz * dirZ;
        double lenSq1 = dx * dx + dz * dz;
        double lenSq2 = dirX * dirX + dirZ * dirZ;
        
        if (lenSq2 == 0 || lenSq1 == 0) {
            return Math.abs(dx) <= width && Math.abs(dz) <= width;
        }
        
        double angle = Math.acos(dotProduct / Math.sqrt(lenSq1 * lenSq2));
        
        // Convert width to angle in radians
        double maxAngle = Math.PI * width / 8;
        
        return angle <= maxAngle || Math.sqrt(lenSq1) <= width;
    }
    
    /**
     * Calculate priority for a chunk based on various factors
     * @param distancePriority Base priority based on distance from player
     * @param centerOffset How far from center of the cone
     * @param speed Player movement speed
     * @return The calculated priority
     */
    private int calculatePriority(int distancePriority, int centerOffset, double speed) {
        // Higher priority for chunks:
        // - closer to center of cone
        // - closer to player
        // - when player is moving faster
        
        int centerFactor = Math.max(3 - centerOffset, 1);
        int speedFactor = Math.min((int)(speed * speedInfluenceFactor), 3);
        
        return distancePriority * 3 + centerFactor + speedFactor;
    }
    
    /**
     * Calculate cone properties based on player movement history and velocity
     * @param player The player
     * @return A ConeProperties object containing direction, width, and distance
     */
    private ConeProperties calculateConeProperties(Player player) {
        ConeProperties cone = new ConeProperties();
        
        // Default to player's facing direction if adaptive prediction is disabled
        if (!adaptiveConePrediction) {
            setDirectionFromFacing(player.getFacing(), cone);
            cone.width = baseChunkWidth;
            cone.maxDistance = frontierMaxDistance;
            return cone;
        }
        
        // Try to predict movement from history
        List<PlayerMovement> history = playerMovementHistory.get(player);
        if (history == null || history.size() < 2) {
            // Not enough history, use facing direction
            setDirectionFromFacing(player.getFacing(), cone);
            cone.width = baseChunkWidth;
            cone.maxDistance = frontierMaxDistance;
            return cone;
        }
        
        // Calculate average velocity from history
        Vector avgVelocity = new Vector(0, 0, 0);
        PlayerMovement latest = history.get(history.size() - 1);
        PlayerMovement oldest = history.get(0);
        
        // Only include horizontal movement
        double dx = latest.x - oldest.x;
        double dz = latest.z - oldest.z;
        
        // Consider recent instantaneous velocity too
        Vector velocity = player.getVelocity();
        
        // Calculate time difference in seconds
        double timeDiff = (latest.timestamp - oldest.timestamp) / 1000.0;
        if (timeDiff > 0) {
            avgVelocity.setX(dx / timeDiff);
            avgVelocity.setZ(dz / timeDiff);
        }
        
        // Combine historical average with current velocity (weighted)
        avgVelocity.add(velocity.multiply(0.3));
        
        // Calculate speed in blocks/second (horizontal only)
        double speed = Math.sqrt(avgVelocity.getX() * avgVelocity.getX() + avgVelocity.getZ() * avgVelocity.getZ());
        
        // Set direction from velocity if it's significant, otherwise use facing
        if (speed > 0.5) {
            // Normalize to get direction vector
            cone.directionX = avgVelocity.getX() / speed;
            cone.directionZ = avgVelocity.getZ() / speed;
        } else {
            // Not moving fast enough, use facing direction
            setDirectionFromFacing(player.getFacing(), cone);
        }
        
        // Adjust cone width and distance based on speed
        cone.width = baseChunkWidth + (int)(speed / 2);
        cone.maxDistance = frontierMaxDistance + (int)(speed / 3);
        cone.speed = speed;
        
        return cone;
    }
    
    /**
     * Set direction vector based on player's facing direction
     * @param facing The player's BlockFace direction
     * @param cone The cone properties to update
     */
    private void setDirectionFromFacing(BlockFace facing, ConeProperties cone) {
        switch (facing) {
            case NORTH:
                cone.directionX = 0;
                cone.directionZ = -1;
                break;
            case SOUTH:
                cone.directionX = 0;
                cone.directionZ = 1;
                break;
            case EAST:
                cone.directionX = 1;
                cone.directionZ = 0;
                break;
            case WEST:
                cone.directionX = -1;
                cone.directionZ = 0;
                break;
            case NORTH_EAST:
                cone.directionX = 0.7071; // 1/sqrt(2)
                cone.directionZ = -0.7071;
                break;
            case NORTH_WEST:
                cone.directionX = -0.7071;
                cone.directionZ = -0.7071;
                break;
            case SOUTH_EAST:
                cone.directionX = 0.7071;
                cone.directionZ = 0.7071;
                break;
            case SOUTH_WEST:
                cone.directionX = -0.7071;
                cone.directionZ = 0.7071;
                break;
            default:
                cone.directionX = 0;
                cone.directionZ = 0;
                break;
        }
    }

    /**
     * Add a chunk to the loading queue if it's not already queued
     * @param world The world
     * @param x The chunk X coordinate
     * @param z The chunk Z coordinate
     */
    private void addToQueue(World world, int x, int z) {
        // Default priority of 1
        addToQueue(world, x, z, 1);
    }
    
    /**
     * Add a chunk to the loading queue with specified priority if not already queued
     * @param world The world
     * @param x The chunk X coordinate
     * @param z The chunk Z coordinate
     * @param priority Priority for loading (higher values = higher priority)
     */
    private void addToQueue(World world, int x, int z, int priority) {
        ChunkCoordinate coord = new ChunkCoordinate(world, x, z);
        
        synchronized (queuedChunks) {
            if (!queuedChunks.contains(coord)) {
                queuedChunks.add(coord);
                PrioritizedChunk prioritizedChunk = new PrioritizedChunk(coord, priority);
                chunkQueue.offer(prioritizedChunk);
            }
        }
    }
    
    /**
     * Get the current size of the chunk queue
     * @return The number of chunks in the queue
     */
    public int getQueueSize() {
        return chunkQueue.size();
    }
    
    /**
     * Get performance statistics for display
     * @return Map with performance statistics
     */
    public Map<String, Object> getPerformanceStats() {
        Map<String, Object> stats = new HashMap<>();
        
        stats.put("totalChunksLoaded", totalChunksLoaded.get());
        stats.put("chunksLoadedLastMinute", chunksLoadedThisMinute.get());
        stats.put("currentQueueSize", chunkQueue.size());
        
        // Calculate average load time from recent chunks
        if (!chunkLoadTimes.isEmpty()) {
            double avgLoadTime = chunkLoadTimes.values().stream()
                    .mapToLong(Long::longValue)
                    .average()
                    .orElse(0);
            stats.put("averageLoadTimeMs", avgLoadTime);
        } else {
            stats.put("averageLoadTimeMs", 0.0);
        }
        
        // TPS information
        double currentTps = plugin.getServer().getTPS()[0];
        stats.put("currentTps", currentTps);
        
        // Adaptive settings status
        if (adaptiveTpsScaling) {
            double tpsRatio = Math.min(currentTps / 20.0, 1.0);
            int effectiveChunksPerTick = tpsRatio < (minTps / 20.0) 
                ? Math.max(1, (int)(maxChunksPerTick * (tpsRatio * 0.8)))
                : maxChunksPerTick;
            stats.put("effectiveChunksPerTick", effectiveChunksPerTick);
        }
        
        return stats;
    }

    /**
     * Class to represent chunk coordinates with world reference
     */
    private static class ChunkCoordinate {
        private final World world;
        private final int x;
        private final int z;

        public ChunkCoordinate(World world, int x, int z) {
            this.world = world;
            this.x = x;
            this.z = z;
        }

        public World getWorld() {
            return world;
        }

        public int getX() {
            return x;
        }

        public int getZ() {
            return z;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ChunkCoordinate that = (ChunkCoordinate) o;

            if (x != that.x) return false;
            if (z != that.z) return false;
            return world.equals(that.world);
        }

        @Override
        public int hashCode() {
            int result = world.hashCode();
            result = 31 * result + x;
            result = 31 * result + z;
            return result;
        }
    }

    /**
     * Class to represent a chunk with loading priority
     */
    private static class PrioritizedChunk {
        private final ChunkCoordinate coordinate;
        private final int priority; // Higher number = higher priority
        private final long timestamp;

        public PrioritizedChunk(ChunkCoordinate coordinate, int priority) {
            this.coordinate = coordinate;
            this.priority = priority;
            this.timestamp = System.currentTimeMillis();
        }

        public ChunkCoordinate getCoordinate() {
            return coordinate;
        }

        public int getPriority() {
            return priority;
        }

        public long getTimestamp() {
            return timestamp;
        }
    }
    
    /**
     * Class to represent player movement history
     */
    private static class PlayerMovement {
        private final World world;
        private final double x;
        private final double y;
        private final double z;
        private final Vector velocity;
        private final long timestamp;
        
        public PlayerMovement(World world, double x, double y, double z, Vector velocity, long timestamp) {
            this.world = world;
            this.x = x;
            this.y = y;
            this.z = z;
            this.velocity = velocity.clone();
            this.timestamp = timestamp;
        }
    }
    
    /**
     * Class to hold cone properties for chunk loading
     */
    private static class ConeProperties {
        double directionX = 0;
        double directionZ = 0;
        int width = 1;
        int maxDistance = 3;
        double speed = 0;
    }
}
