package me.remag501.reputation.manager;

import me.remag501.reputation.Reputation;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PermissionManager {

    private final Map<String, Map<String, Integer>> dealerPermissions = new HashMap<>(); // dealer -> (perm -> minRep)
    private final Map<UUID, PermissionAttachment> attachments = new HashMap<>(); // player -> attachment
    private final Plugin plugin;

    public PermissionManager(Plugin plugin, FileConfiguration configuration) {
        this.plugin = plugin; // used to attach permissions
        reload(configuration);
    }

    /** Loads dealer permission thresholds from config.yml */
    public void reload(FileConfiguration config) {
        dealerPermissions.clear();

        if (!config.isConfigurationSection("permissions")) return;

        for (String dealer : config.getConfigurationSection("permissions").getKeys(false)) {
            List<Map<?, ?>> permList = config.getMapList("permissions." + dealer);
            Map<String, Integer> permMap = new HashMap<>();

            for (Map<?, ?> entry : permList) {
                String permission = (String) entry.get("permission");
                int min = (int) entry.get("min");
                permMap.put(permission, min);
            }

            dealerPermissions.put(dealer.toLowerCase(), permMap);
        }
    }

    /**
     * Ensure player has an attachment in the map (creates if missing).
     */
    private PermissionAttachment getAttachment(Player player) {
        return attachments.computeIfAbsent(player.getUniqueId(), uuid ->
                player.addAttachment(plugin));
    }

    /**
     * Apply or revoke permissions for a specific dealer based on player's reputation.
     */
    public void applyPermissions(Player player, String dealer, int playerRep) {
        dealer = dealer.toLowerCase();

        Map<String, Integer> perms = dealerPermissions.get(dealer);
        if (perms == null) return;

        PermissionAttachment attachment = getAttachment(player);

        for (Map.Entry<String, Integer> entry : perms.entrySet()) {
            String permission = entry.getKey();
            int requiredRep = entry.getValue();

            if (playerRep >= requiredRep) {
                // Grant
                attachment.setPermission(permission, true);
            } else {
                // Revoke
                attachment.unsetPermission(permission);
            }
        }
    }

    /**
     * Apply all dealer permissions for player (used on join or reload).
     */
    public void applyAllPermissions(Player player, Map<String, Integer> playerRepMap) {
        for (Map.Entry<String, Map<String, Integer>> dealerEntry : dealerPermissions.entrySet()) {
            String dealer = dealerEntry.getKey();
            int rep = playerRepMap.getOrDefault(dealer, 0);
            applyPermissions(player, dealer, rep);
        }
    }

    /**
     * Cleanup attachment when player leaves.
     */
    public void removeAttachment(Player player) {
        PermissionAttachment attachment = attachments.remove(player.getUniqueId());
        if (attachment != null) {
            attachment.remove();
        }
    }

    /** Optional: clear everything on plugin disable */
    public void clearAll() {
        for (PermissionAttachment attachment : attachments.values()) {
            attachment.remove();
        }
        attachments.clear();
    }
}
