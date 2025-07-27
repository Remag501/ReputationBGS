package me.remag501.reputation.util;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PermissionUtil {

    // dealer -> (permission -> min reputation)
    private final Map<String, Map<String, Integer>> dealerPermissions = new HashMap<>();

    /**
     * Add a permission requirement for a dealer.
     *
     * @param dealer Dealer name (NPC)
     * @param permission Permission string (e.g., "trader.buy.sword")
     * @param minReputation Minimum reputation required
     */
    public void addPermission(String dealer, String permission, int minReputation) {
        dealerPermissions
                .computeIfAbsent(dealer.toLowerCase(), k -> new HashMap<>())
                .put(permission, minReputation);
    }

    /**
     * Get the minimum reputation required for a dealer's permission.
     *
     * @return min reputation or -1 if no requirement set
     */
    public int getMinReputation(String dealer, String permission) {
        Map<String, Integer> perms = dealerPermissions.get(dealer.toLowerCase());
        if (perms == null) return -1;
        return perms.getOrDefault(permission, -1);
    }

    /**
     * Check if a player's reputation meets the requirement.
     *
     * @param playerRep Player's reputation with the dealer
     * @param dealer Dealer name
     * @param permission Permission string
     * @return true if meets or no requirement exists
     */
    public boolean canAccess(int playerRep, String dealer, String permission) {
        int required = getMinReputation(dealer, permission);
        if (required == -1) return true; // no requirement
        return playerRep >= required;
    }

    /**
     * Load from config
     * Example YAML structure:
     * permissions:
     *   blacksmith:
     *     trader.buy.sword: 10
     *     trader.sell.armor: 20
     *   alchemist:
     *     trader.buy.potion: 5
     */
    public void loadFromConfig(org.bukkit.configuration.file.FileConfiguration config) {
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

            dealerPermissions.put(dealer, permMap);
        }
    }

    /**
     * Save to config (overwrite current section)
     */
    public void saveToConfig(org.bukkit.configuration.file.FileConfiguration config) {
        config.set("permissions", null);

        for (Map.Entry<String, Map<String, Integer>> dealerEntry : dealerPermissions.entrySet()) {
            String dealer = dealerEntry.getKey();
            for (Map.Entry<String, Integer> permEntry : dealerEntry.getValue().entrySet()) {
                config.set("permissions." + dealer + "." + permEntry.getKey(), permEntry.getValue());
            }
        }
    }

    public void applyPermissions(Player player, String dealer, int playerRep) {
        Map<String, Integer> perms = dealerPermissions.get(dealer.toLowerCase());
        if (perms == null) return;
        for (Map.Entry<String, Integer> entry : perms.entrySet()) {
            String permission = entry.getKey();
            int requiredRep = entry.getValue();

            if (playerRep >= requiredRep) {
                // Grant
                player.addAttachment(Bukkit.getPluginManager().getPlugin("Reputation"), permission, true);
            } else {
                // Revoke
                player.addAttachment(Bukkit.getPluginManager().getPlugin("Reputation"), permission, false);
            }
        }
    }

}

