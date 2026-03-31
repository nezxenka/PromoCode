package org.nezxenka.promocode.config;

import lombok.Getter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.nezxenka.promocode.PromoCode;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

@Getter
public class DatabaseConfig {

    private final PromoCode plugin;
    private File configFile;
    private FileConfiguration config;

    public DatabaseConfig(PromoCode plugin) {
        this.plugin = plugin;
        createConfig();
        reload();
    }

    private void createConfig() {
        configFile = new File(plugin.getDataFolder(), "database.yml");
        
        if (!configFile.exists()) {
            try {
                configFile.getParentFile().mkdirs();
                InputStream in = plugin.getResource("database.yml");
                if (in != null) {
                    Files.copy(in, configFile.toPath());
                }
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create database.yml!");
                e.printStackTrace();
            }
        }
    }

    public void reload() {
        config = YamlConfiguration.loadConfiguration(configFile);
    }

    public String getDatabaseType() {
        return config.getString("type", "MYSQL");
    }

    public String getMySQLHost() {
        return config.getString("mysql.host", "localhost");
    }

    public int getMySQLPort() {
        return config.getInt("mysql.port", 3306);
    }

    public String getMySQLDatabase() {
        return config.getString("mysql.database", "promocode");
    }

    public String getMySQLUser() {
        return config.getString("mysql.user", "root");
    }

    public String getMySQLPassword() {
        return config.getString("mysql.password", "");
    }

    public int getMaximumPoolSize() {
        return config.getInt("hikari.maximum-pool-size", 10);
    }

    public int getMinimumIdle() {
        return config.getInt("hikari.minimum-idle", 2);
    }

    public long getMaxLifetime() {
        return config.getLong("hikari.max-lifetime", 1800000);
    }

    public long getConnectionTimeout() {
        return config.getLong("hikari.connection-timeout", 5000);
    }

    public long getIdleTimeout() {
        return config.getLong("hikari.idle-timeout", 600000);
    }

    public long getKeepaliveTime() {
        return config.getLong("hikari.keepalive-time", 300000);
    }

    public String getSQLiteFilename() {
        return config.getString("sqlite.filename", "promocodes.db");
    }
}
