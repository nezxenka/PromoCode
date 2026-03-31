package org.nezxenka.promocode.config;

import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.nezxenka.promocode.PromoCode;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public class MessageConfig {

    private final PromoCode plugin;
    private File configFile;
    private FileConfiguration config;

    public MessageConfig(PromoCode plugin) {
        this.plugin = plugin;
        createConfig();
        reload();
    }

    private void createConfig() {
        configFile = new File(plugin.getDataFolder(), "messages.yml");
        
        if (!configFile.exists()) {
            try {
                configFile.getParentFile().mkdirs();
                InputStream in = plugin.getResource("messages.yml");
                if (in != null) {
                    Files.copy(in, configFile.toPath());
                }
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create messages.yml!");
                e.printStackTrace();
            }
        }
    }

    public void reload() {
        config = YamlConfiguration.loadConfiguration(configFile);
    }

    public String getMessage(String path) {
        String message = config.getString(path, "Message not found: " + path);
        return colorize(message);
    }

    public List<String> getMessageList(String path) {
        return config.getStringList(path).stream()
                .map(this::colorize)
                .collect(Collectors.toList());
    }

    public void sendMessage(Player player, String path) {
        player.sendMessage(getMessage(path));
    }

    public void sendMessageList(Player player, String path) {
        getMessageList(path).forEach(player::sendMessage);
    }

    public String colorize(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public String getPrefix() {
        return getMessage("general.prefix");
    }

    public String getNoPermission() {
        return getMessage("general.no-permission");
    }

    public String getPlayerOnly() {
        return getMessage("general.player-only");
    }

    public String getReloadSuccess() {
        return getMessage("general.reload-success");
    }

    public String getAlreadyActivated() {
        return getMessage("activation.already-activated");
    }

    public String getAlreadyActivatedGroup() {
        return getMessage("activation.already-activated-group");
    }

    public String getMaxUsesReached() {
        return getMessage("activation.max-uses-reached");
    }

    public String getNotExists() {
        return getMessage("activation.not-exists");
    }

    public List<String> getNeedLinkMessage() {
        return getMessageList("activation.need-link");
    }
}
