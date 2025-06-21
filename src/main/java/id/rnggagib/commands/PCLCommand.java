package id.rnggagib.commands;

import id.rnggagib.logic.ChunkLoadManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Handles commands for ProactiveChunkLoader
 */
public class PCLCommand implements CommandExecutor, TabCompleter {

    private final JavaPlugin plugin;
    private final ChunkLoadManager chunkLoadManager;    public PCLCommand(JavaPlugin plugin, ChunkLoadManager chunkLoadManager) {
        this.plugin = plugin;
        this.chunkLoadManager = chunkLoadManager;
    }
      @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            showHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload":
                if (!sender.hasPermission("proactivechunkloader.admin")) {
                    sender.sendMessage(Component.text("You don't have permission to use this command.").color(NamedTextColor.RED));
                    return true;
                }
                plugin.reloadConfig();
                chunkLoadManager.loadConfig();
                sender.sendMessage(Component.text("ProactiveChunkLoader configuration reloaded.").color(NamedTextColor.GREEN));
                return true;
            case "info":
                sender.sendMessage(
                    Component.text("===== ").color(NamedTextColor.GOLD)
                        .append(Component.text("ProactiveChunkLoader Info").color(NamedTextColor.GREEN))
                        .append(Component.text(" =====").color(NamedTextColor.GOLD))
                );
                sender.sendMessage(
                    Component.text("Version: ").color(NamedTextColor.GREEN)
                        .append(Component.text(plugin.getPluginMeta().getVersion()).color(NamedTextColor.WHITE))
                );
                sender.sendMessage(
                    Component.text("Queued chunks: ").color(NamedTextColor.GREEN)
                        .append(Component.text(String.valueOf(chunkLoadManager.getQueueSize())).color(NamedTextColor.WHITE))
                );
                sender.sendMessage(
                    Component.text("Debug mode: ").color(NamedTextColor.GREEN)
                        .append(Component.text(plugin.getConfig().getBoolean("debug") ? "Enabled" : "Disabled").color(NamedTextColor.WHITE))
                );
                return true;
            case "stats":
                if (!sender.hasPermission("proactivechunkloader.stats")) {
                    sender.sendMessage(Component.text("You don't have permission to use this command.").color(NamedTextColor.RED));
                    return true;
                }
                showStats(sender);
                return true;
            case "help":
            default:
                showHelp(sender);
                return true;
        }
    }    private void showHelp(CommandSender sender) {
        sender.sendMessage(
            Component.text("===== ").color(NamedTextColor.GOLD)
                .append(Component.text("ProactiveChunkLoader Commands").color(NamedTextColor.GREEN))
                .append(Component.text(" =====").color(NamedTextColor.GOLD))
        );
        sender.sendMessage(
            Component.text("/pcl help").color(NamedTextColor.GREEN)
                .append(Component.text(" - Show this help message").color(NamedTextColor.WHITE))
        );
        sender.sendMessage(
            Component.text("/pcl info").color(NamedTextColor.GREEN)
                .append(Component.text(" - Show plugin info").color(NamedTextColor.WHITE))
        );
        if (sender.hasPermission("proactivechunkloader.stats")) {
            sender.sendMessage(
                Component.text("/pcl stats").color(NamedTextColor.GREEN)
                    .append(Component.text(" - Show performance statistics").color(NamedTextColor.WHITE))
            );
        }
        if (sender.hasPermission("proactivechunkloader.admin")) {
            sender.sendMessage(
                Component.text("/pcl reload").color(NamedTextColor.GREEN)
                    .append(Component.text(" - Reload the plugin configuration").color(NamedTextColor.WHITE))
            );
        }
    }    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            List<String> subCommands = new ArrayList<>(Arrays.asList("help", "info"));
            if (sender.hasPermission("proactivechunkloader.stats")) {
                subCommands.add("stats");
            }
            if (sender.hasPermission("proactivechunkloader.admin")) {
                subCommands.add("reload");
            }
            
            String partialCommand = args[0].toLowerCase();
            completions = subCommands.stream()
                .filter(cmd -> cmd.startsWith(partialCommand))
                .collect(Collectors.toList());
        }
        
        return completions;
    }

    /**
     * Show performance statistics to the sender
     * @param sender The command sender
     */
    private void showStats(CommandSender sender) {
        Map<String, Object> stats = chunkLoadManager.getPerformanceStats();
        
        sender.sendMessage(
            Component.text("===== ").color(NamedTextColor.GOLD)
                .append(Component.text("ProactiveChunkLoader Stats").color(NamedTextColor.GREEN))
                .append(Component.text(" =====").color(NamedTextColor.GOLD))
        );
        
        sender.sendMessage(
            Component.text("Total chunks loaded: ").color(NamedTextColor.GREEN)
                .append(Component.text(String.valueOf(stats.get("totalChunksLoaded"))).color(NamedTextColor.WHITE))
        );
        
        sender.sendMessage(
            Component.text("Chunks loaded (last minute): ").color(NamedTextColor.GREEN)
                .append(Component.text(String.valueOf(stats.get("chunksLoadedLastMinute"))).color(NamedTextColor.WHITE))
        );
        
        sender.sendMessage(
            Component.text("Current queue size: ").color(NamedTextColor.GREEN)
                .append(Component.text(String.valueOf(stats.get("currentQueueSize"))).color(NamedTextColor.WHITE))
        );
        
        sender.sendMessage(
            Component.text("Average load time: ").color(NamedTextColor.GREEN)
                .append(Component.text(String.format("%.2f ms", stats.get("averageLoadTimeMs"))).color(NamedTextColor.WHITE))
        );
        
        // Display TPS with color indication
        double tps = (Double) stats.get("currentTps");
        NamedTextColor tpsColor = NamedTextColor.GREEN;
        if (tps < 18) tpsColor = NamedTextColor.YELLOW;
        if (tps < 16) tpsColor = NamedTextColor.GOLD;
        if (tps < 12) tpsColor = NamedTextColor.RED;
        
        sender.sendMessage(
            Component.text("Current TPS: ").color(NamedTextColor.GREEN)
                .append(Component.text(String.format("%.2f", tps)).color(tpsColor))
        );
        
        if (stats.containsKey("effectiveChunksPerTick")) {
            sender.sendMessage(
                Component.text("Adaptive chunks per tick: ").color(NamedTextColor.GREEN)
                    .append(Component.text(String.valueOf(stats.get("effectiveChunksPerTick"))).color(NamedTextColor.WHITE))
            );
        }
    }
}
