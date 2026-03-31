package org.nezxenka.promocode;

import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;
import org.nezxenka.promocode.command.PromoCodeCommand;
import org.nezxenka.promocode.config.ConfigManager;
import org.nezxenka.promocode.config.DatabaseConfig;
import org.nezxenka.promocode.config.MessageConfig;
import org.nezxenka.promocode.database.DatabaseManager;
import org.nezxenka.promocode.listener.PromoCodeListener;
import org.nezxenka.promocode.service.PromoCodeService;

@Getter
public final class PromoCode extends JavaPlugin {

    private ConfigManager configManager;
    private DatabaseConfig databaseConfig;
    private MessageConfig messageConfig;
    private DatabaseManager databaseManager;
    private PromoCodeService promoCodeService;

    @Override
    public void onEnable() {
        long startTime = System.currentTimeMillis();
        
        getLogger().info("=================================");
        getLogger().info("  PromoCode Plugin");
        getLogger().info("  Version: " + getDescription().getVersion());
        getLogger().info("  Author: nezxenka");
        getLogger().info("=================================");

        if (!initializeConfigs()) {
            getLogger().severe("Failed to initialize configs! Disabling plugin...");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        if (!initializeDatabase()) {
            getLogger().severe("Failed to initialize database! Disabling plugin...");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        initializeServices();

        registerCommands();
        registerListeners();

        long loadTime = System.currentTimeMillis() - startTime;
        getLogger().info("Plugin enabled successfully in " + loadTime + "ms!");
    }

    @Override
    public void onDisable() {
        getLogger().info("Disabling PromoCode plugin...");

        if (databaseManager != null) {
            databaseManager.shutdown();
            getLogger().info("Database connection closed.");
        }

        getLogger().info("PromoCode plugin disabled!");
    }

    private boolean initializeConfigs() {
        try {
            saveDefaultConfig();
            
            configManager = new ConfigManager(this);
            databaseConfig = new DatabaseConfig(this);
            messageConfig = new MessageConfig(this);

            getLogger().info("Configurations loaded successfully!");
            return true;
        } catch (Exception e) {
            getLogger().severe("Error loading configurations: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private boolean initializeDatabase() {
        try {
            databaseManager = new DatabaseManager(this, databaseConfig);
            databaseManager.initialize();
            getLogger().info("Database initialized successfully!");
            return true;
        } catch (Exception e) {
            getLogger().severe("Error initializing database: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private void initializeServices() {
        promoCodeService = new PromoCodeService(this, databaseManager, configManager, messageConfig);
        getLogger().info("Services initialized successfully!");
    }

    private void registerCommands() {
        PromoCodeCommand command = new PromoCodeCommand(this, promoCodeService, messageConfig);
        getCommand("promocode").setExecutor(command);
        getCommand("promocode").setTabCompleter(command);
        getLogger().info("Commands registered successfully!");
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(
            new PromoCodeListener(this, promoCodeService, messageConfig),
            this
        );
        getLogger().info("Listeners registered successfully!");
    }

    public void reload() {
        configManager.reload();
        databaseConfig.reload();
        messageConfig.reload();
        promoCodeService.reload();
    }
}