package id.rnggagib;

import id.rnggagib.commands.PCLCommand;
import id.rnggagib.listeners.PlayerMoveListener;
import id.rnggagib.logic.ChunkLoadManager;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * ProactiveChunkLoader main plugin class
 * This plugin proactively loads chunks in the direction players are moving
 * to reduce lag spikes when exploring new areas
 */
public class Plugin extends JavaPlugin {
    private ChunkLoadManager chunkLoadManager;
    
    @Override
    public void onEnable() {
        // Save default configuration
        saveDefaultConfig();
        
        
        // Initialize chunk load manager
        chunkLoadManager = new ChunkLoadManager(this);
        
        // Register event listeners
        getServer().getPluginManager().registerEvents(new PlayerMoveListener(chunkLoadManager), this);
        
        // Register commands
        PCLCommand pclCommand = new PCLCommand(this, chunkLoadManager);
        PluginCommand command = getCommand("pcl");
        if (command != null) {
            command.setExecutor(pclCommand);
            command.setTabCompleter(pclCommand);
        }
        
        // Start the chunk loading task
        chunkLoadManager.startTask();
        
        getLogger().info("ProactiveChunkLoader v" + this.getPluginMeta().getVersion() + " activated");
        getLogger().info("Pre-loading chunks in players' path to reduce lag spikes");
    }

    @Override
    public void onDisable() {
        // Ensure task is stopped
        if (chunkLoadManager != null) {
            chunkLoadManager.stopTask();
        }
        
        getLogger().info("ProactiveChunkLoader deactivated");
    }
}
