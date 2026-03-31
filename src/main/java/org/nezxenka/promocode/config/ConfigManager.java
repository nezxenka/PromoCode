package org.nezxenka.promocode.config;

import lombok.Getter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.nezxenka.promocode.PromoCode;
import org.nezxenka.promocode.model.PromoCodeData;

import java.util.*;

@Getter
public class ConfigManager {

    private final PromoCode plugin;
    private FileConfiguration config;
    private final Map<String, PromoCodeData> promoCodes;

    public ConfigManager(PromoCode plugin) {
        this.plugin = plugin;
        this.promoCodes = new HashMap<>();
        reload();
    }

    public void reload() {
        plugin.reloadConfig();
        config = plugin.getConfig();
        loadPromoCodes();
    }

    private void loadPromoCodes() {
        promoCodes.clear();
        
        ConfigurationSection promoSection = config.getConfigurationSection("promocodes");
        if (promoSection == null) {
            plugin.getLogger().warning("No promocodes section found in config.yml!");
            return;
        }

        for (String key : promoSection.getKeys(false)) {
            ConfigurationSection promoConfig = promoSection.getConfigurationSection(key);
            if (promoConfig == null) continue;

            PromoCodeData data = PromoCodeData.builder()
                    .code(key.toLowerCase())
                    .playerUses(promoConfig.getInt("player_uses", 1))
                    .globalUses(promoConfig.getInt("global_uses", -1))
                    .group(promoConfig.getString("group", "default"))
                    .needLink(promoConfig.getBoolean("needLink", false))
                    .messages(promoConfig.getStringList("messages"))
                    .commands(promoConfig.getStringList("commands"))
                    .build();

            promoCodes.put(key.toLowerCase(), data);

            // Загрузка алиасов
            if (promoConfig.contains("aliases")) {
                List<String> aliases = promoConfig.getStringList("aliases");
                for (String alias : aliases) {
                    promoCodes.put(alias.toLowerCase(), data);
                }
            }
        }

        plugin.getLogger().info("Loaded " + promoCodes.size() + " promocodes!");
    }

    public PromoCodeData getPromoCode(String code) {
        return promoCodes.get(code.toLowerCase());
    }

    public boolean isPromoCodeExists(String code) {
        return promoCodes.containsKey(code.toLowerCase());
    }

    public Set<String> getAllPromoCodes() {
        return new HashSet<>(promoCodes.keySet());
    }

    public boolean isDebugEnabled() {
        return config.getBoolean("settings.debug", false);
    }

    public boolean isCheckUpdates() {
        return config.getBoolean("settings.check-updates", true);
    }
}
