package me.remag501.reputation.core;

import me.remag501.reputation.util.PermissionUtil;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.*;

public class ReputationCore {

    private final Map<UUID, Map<String, Integer>> playersReputation = new HashMap<>();
    private final List<String> npcList;
    private final FileConfiguration reputationData;
    private final PermissionUtil permissionUtil;
    private final double buyRate;
    private final double sellRate;

    public ReputationCore(FileConfiguration reputationData, List<String> npcList, PermissionUtil permissionUtil) {
        this.reputationData = reputationData;
        this.npcList = npcList;
        this.permissionUtil = permissionUtil;

        // Read from main config (not reputation.yml)
        FileConfiguration mainConfig = Bukkit.getPluginManager().getPlugin("Reputation").getConfig();
        this.buyRate = mainConfig.getDouble("conversion.buy-rate", 0.5);
        this.sellRate = mainConfig.getDouble("conversion.sell-rate", 0.2);

        loadReputation();
    }

    public boolean isValidNpc(String npc) {
        return npcList.contains(npc.toLowerCase());
    }

    private Map<String, Integer> getPlayerMap(UUID uuid) {
        return playersReputation.computeIfAbsent(uuid, k -> new HashMap<>());
    }

    // --- Reputation modification methods ---

    public void addReputation(Player player, String npc, int amount) {
        if (!isValidNpc(npc)) return;
        Map<String, Integer> repMap = getPlayerMap(player.getUniqueId());
        int newRep = repMap.getOrDefault(npc, 0) + amount;
        repMap.put(npc, newRep);
        saveReputation();
        permissionUtil.applyPermissions(player, npc, newRep); // Apply permissions
    }

    public void removeReputation(Player player, String npc, int amount) {
        if (!isValidNpc(npc)) return;
        Map<String, Integer> repMap = getPlayerMap(player.getUniqueId());
        int newRep = Math.max(0, repMap.getOrDefault(npc, 0) - amount);
        repMap.put(npc, newRep);
        saveReputation();
        permissionUtil.applyPermissions(player, npc, newRep); // Apply permissions
    }

    public void setReputation(Player player, String npc, int value) {
        if (!isValidNpc(npc)) return;
        getPlayerMap(player.getUniqueId()).put(npc, value);
        saveReputation();
        permissionUtil.applyPermissions(player, npc, value); // Apply permissions
    }

    public int getReputation(Player player, String npc) {
        if (!isValidNpc(npc)) return 0;
        return getPlayerMap(player.getUniqueId()).getOrDefault(npc, 0);
    }

    // --- Load and save methods ---

    public void loadReputation() {
        playersReputation.clear();

        if (!reputationData.isConfigurationSection("reputation")) return;

        for (String uuidString : reputationData.getConfigurationSection("reputation").getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(uuidString);
                Map<String, Integer> repMap = new HashMap<>();

                for (String npc : npcList) {
                    int value = reputationData.getInt("reputation." + uuidString + "." + npc, 0);
                    repMap.put(npc, value);
                }
                playersReputation.put(uuid, repMap);
            } catch (IllegalArgumentException e) {
                Bukkit.getLogger().warning("Invalid UUID in reputation file: " + uuidString);
            }
        }
    }

    public void saveReputation() {
        reputationData.set("reputation", null);

        for (Map.Entry<UUID, Map<String, Integer>> playerEntry : playersReputation.entrySet()) {
            String uuidString = playerEntry.getKey().toString();
            for (Map.Entry<String, Integer> npcEntry : playerEntry.getValue().entrySet()) {
                reputationData.set("reputation." + uuidString + "." + npcEntry.getKey(), npcEntry.getValue());
            }
        }
    }

    public double getBuyRate() {
        return buyRate;
    }

    public double getSellRate() {
        return sellRate;
    }

    public List<String> getNpcList() {
        return npcList;
    }
}
