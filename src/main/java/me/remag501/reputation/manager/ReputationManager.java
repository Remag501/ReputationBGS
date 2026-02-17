package me.remag501.reputation.manager;

import me.remag501.reputation.Reputation;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.*;

public class ReputationManager {

    private final DealerManager dealerManager;
    private final PermissionManager permissionManager;
    private final Map<UUID, Map<String, Integer>> playersReputation = new HashMap<>();
    private final FileConfiguration reputationData;

    private List<String> npcList;
    private double buyRate;
    private double sellRate;
    private String msgGain;
    private String msgLoss;
    private Sound soundGain;
    private Sound soundLoss;


    public ReputationManager(DealerManager dealerManager, PermissionManager permissionManager, FileConfiguration reputationData, FileConfiguration mainConfig) {
        this.dealerManager = dealerManager;
        this.permissionManager = permissionManager;
        this.reputationData = reputationData;
        reload(mainConfig);
    }

    public void reload(FileConfiguration mainConfig) {
        npcList = dealerManager.getDealers();
        loadConfig(mainConfig);
        loadReputation();
    }

    public Map<String, Integer> getReputationMap(Player player) {
        UUID uuid = player.getUniqueId();
        // Return a copy to avoid accidental modifications
        return new HashMap<>(playersReputation.getOrDefault(uuid, new HashMap<>()));
    }

    private void loadConfig(FileConfiguration mainConfig) {
        // Read from main config (not reputation.yml)
        this.buyRate = mainConfig.getDouble("conversion.buy-rate", 0.5);
        this.sellRate = mainConfig.getDouble("conversion.sell-rate", 0.2);

        // load custom messages and sounds
        this.msgGain = mainConfig.getString("messages.reputation-gain", "&aYou gained &e%amount% rep with &b%dealer%!");
        this.msgLoss = mainConfig.getString("messages.reputation-loss", "&cYou lost &e%amount% rep with &b%dealer%!");

        this.soundGain = getSoundFromConfig("sounds.gain", "entity.player.levelup", mainConfig);
        this.soundLoss = getSoundFromConfig("sounds.loss", "entity.villager.no", mainConfig);
    }

    private Sound getSoundFromConfig(String path, String defaultValue, FileConfiguration mainConfig) {
        String name = mainConfig.getString(path, defaultValue).toLowerCase().replace("_", ".");
        NamespacedKey key = NamespacedKey.fromString(name);

        Sound sound = Registry.SOUNDS.get(key);
        if (sound == null) {
            // Fallback or log warning
            return Sound.ENTITY_PLAYER_LEVELUP;
        }
        return sound;
    }

    private void sendFeedback(Player player, int amount, String dealer, boolean isGain) {
        String messageTemplate = isGain ? msgGain : msgLoss;
        String message = messageTemplate
                .replace("%amount%", String.valueOf(amount))
                .replace("%dealer%", dealer);

        player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));

        player.playSound(player.getLocation(), isGain ? soundGain : soundLoss, 1f, 1f);
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
        permissionManager.applyPermissions(player, npc, newRep); // Apply permissions
        sendFeedback(player, amount, npc, true);
    }

    public void removeReputation(Player player, String npc, int amount) {
        if (!isValidNpc(npc)) return;
        Map<String, Integer> repMap = getPlayerMap(player.getUniqueId());
        int newRep = Math.max(0, repMap.getOrDefault(npc, 0) - amount);
        repMap.put(npc, newRep);
        saveReputation();
        permissionManager.applyPermissions(player, npc, newRep); // Apply permissions
        sendFeedback(player, amount, npc, false);
    }

    public void setReputation(Player player, String npc, int value) {
        if (!isValidNpc(npc)) return;
        getPlayerMap(player.getUniqueId()).put(npc, value);
        saveReputation();
        permissionManager.applyPermissions(player, npc, value); // Apply permissions
    }

    public int getReputation(Player player, String npc) {
        if (!isValidNpc(npc)) return 0;
        return getPlayerMap(player.getUniqueId()).getOrDefault(npc, 0);
    }

    public boolean isValidDealer(String dealer) {
        return getNpcList().contains(dealer);
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
