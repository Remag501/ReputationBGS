package me.remag501.reputation.listener;

import me.clip.placeholderapi.PlaceholderAPI;
import me.remag501.reputation.Reputation;
import me.remag501.reputation.manager.PermissionManager;
import me.remag501.reputation.manager.ReputationManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.entity.Player;

public class PlayerListener implements Listener {

    private final PermissionManager permissionManager;
    private final ReputationManager reputationManager;

    public PlayerListener(PermissionManager permissionManager, ReputationManager reputationManager) {
        this.permissionManager = permissionManager;
        this.reputationManager = reputationManager;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        permissionManager.applyAllPermissions(player, reputationManager.getReputationMap(player));
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        permissionManager.removeAttachment(event.getPlayer());
    }

}
