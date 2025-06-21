package id.rnggagib.listeners;

import id.rnggagib.logic.ChunkLoadManager;
import org.bukkit.Chunk;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

/**
 * Listener for player movement to detect chunk changes
 */
public class PlayerMoveListener implements Listener {
    private final ChunkLoadManager chunkLoadManager;

    public PlayerMoveListener(ChunkLoadManager chunkLoadManager) {
        this.chunkLoadManager = chunkLoadManager;
    }

    /**
     * Handle player movement event
     * Only process if player has crossed chunk boundaries
     * @param event The player move event
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        // Check if player moved to a different chunk
        Chunk fromChunk = event.getFrom().getChunk();
        Chunk toChunk = event.getTo().getChunk();
        
        // Only process if player has moved to a different chunk
        if (fromChunk.getX() != toChunk.getX() || fromChunk.getZ() != toChunk.getZ()) {
            Player player = event.getPlayer();
            
            // Queue chunks for loading based on player's direction
            chunkLoadManager.queueChunksForPlayer(player);
        }
    }
}
