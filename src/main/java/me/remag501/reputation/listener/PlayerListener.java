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

//    private final PermissionManager permissionManager;
//    private final ReputationManager reputationManager;
    private final Reputation plugin;

    public PlayerListener(Reputation plugin) {
//        this.permissionManager = plugin.getPermissionManager();
//        this.reputationManager = plugin.getReputationManager();
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        plugin.getPermissionManager().applyAllPermissions(player, plugin.getReputationManager().getReputationMap(player));
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        plugin.getPermissionManager().removeAttachment(event.getPlayer());
    }

}
