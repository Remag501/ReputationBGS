package me.remag501.reputation;

import me.remag501.reputation.command.ReputationCommand;
import me.remag501.reputation.listener.PlayerListener;
import me.remag501.reputation.manager.ReputationManager;
import me.remag501.reputation.manager.DealerManager;
import me.remag501.reputation.manager.PermissionManager;
import me.remag501.reputation.placeholder.ReputationPlaceholder;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

public final class Reputation extends JavaPlugin {

    private ReputationManager reputationManager;
    private File reputationFile;
    private FileConfiguration reputationConfig;
    private DealerManager dealerManager;
    private PermissionManager permissionManager;

    @Override
    public void onEnable() {
        // Load config.yml
        saveDefaultConfig();

        // Setup reputation.yml
        createReputationFile();

        // Example: load NPC list and permission util
        dealerManager = new DealerManager(getConfig());
        permissionManager = new PermissionManager(this, getConfig());
        reputationManager = new ReputationManager(dealerManager, permissionManager, getReputationConfig(), getConfig());

        // Register listener
        getServer().getPluginManager().registerEvents(new PlayerListener(permissionManager, reputationManager), this);

        getCommand("reputation").setExecutor(new ReputationCommand(this, reputationManager));


        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new ReputationPlaceholder(reputationManager).register();
            getLogger().info("PlaceholderAPI hooked successfully.");
        } else {
            getLogger().warning("PlaceholderAPI not detected. Placeholders disabled.");
        }


    }

    public void reload() {
        reloadConfig();
        dealerManager.reload(getConfig());
        permissionManager.reload(getConfig());
        reputationManager.reload(getConfig());
    }

    private void createReputationFile() {
        reputationFile = new File(getDataFolder(), "reputation.yml");

        if (!reputationFile.exists()) {
            try {
                reputationFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        reputationConfig = YamlConfiguration.loadConfiguration(reputationFile);
    }

    public FileConfiguration getReputationConfig() {
        return reputationConfig;
    }

    @Override
    public void onDisable() {
        saveReputationFile();
    }

    public void saveReputationFile() {
        try {
            reputationConfig.save(reputationFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
