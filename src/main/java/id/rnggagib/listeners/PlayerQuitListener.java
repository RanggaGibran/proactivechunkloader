package id.rnggagib.listeners;

import id.rnggagib.logic.ChunkLoadManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Listener for player quit events to clean up resources
 * and prevent memory leaks when players leave the server
 */
public class PlayerQuitListener implements Listener {
    private final ChunkLoadManager chunkLoadManager;

    public PlayerQuitListener(ChunkLoadManager chunkLoadManager) {
        this.chunkLoadManager = chunkLoadManager;
    }

    /**
     * Handle player quit event
     * Clean up resources associated with the player when they leave
     * @param event The player quit event
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        chunkLoadManager.handlePlayerQuit(player);
    }
}
