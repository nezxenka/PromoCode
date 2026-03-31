package org.nezxenka.promocode.service;

import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.nezxenka.promocode.PromoCode;
import org.nezxenka.promocode.config.ConfigManager;
import org.nezxenka.promocode.config.MessageConfig;
import org.nezxenka.promocode.database.DatabaseManager;
import org.nezxenka.promocode.model.PromoCodeData;

import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
public class PromoCodeService {

    private final PromoCode plugin;
    private final DatabaseManager databaseManager;
    private final ConfigManager configManager;
    private final MessageConfig messageConfig;

    public void reload() {
        configManager.reload();
    }

    public CompletableFuture<ActivationResult> activatePromoCode(Player player, String code) {
        return CompletableFuture.supplyAsync(() -> {
            PromoCodeData data = configManager.getPromoCode(code);
            
            if (data == null) {
                return ActivationResult.NOT_EXISTS;
            }

            String playerName = player.getName();
            String ipAddress = player.getAddress().getAddress().getHostAddress();

            if (data.isNeedLink() && !isPlayerLinked(playerName)) {
                return ActivationResult.NEED_LINK;
            }

            if (databaseManager.hasPlayerActivatedGroup(playerName, ipAddress, data.getGroup())) {
                return ActivationResult.ALREADY_ACTIVATED_GROUP;
            }

            if (databaseManager.hasPlayerActivated(playerName, ipAddress, code, data.getPlayerUses())) {
                return ActivationResult.ALREADY_ACTIVATED;
            }

            if (!databaseManager.canActivatePromo(code, data.getGlobalUses())) {
                return ActivationResult.MAX_USES_REACHED;
            }

            databaseManager.activatePromo(playerName, ipAddress, code, data.getGroup());

            executeCommands(player, data);

            sendActivationMessages(player, data, code);

            return ActivationResult.SUCCESS;
        });
    }

    private void executeCommands(Player player, PromoCodeData data) {
        Bukkit.getScheduler().runTask(plugin, () -> {
            for (String command : data.getCommands()) {
                String processedCommand = command.replace("%player%", player.getName());
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), processedCommand);
            }
        });
    }

    private void sendActivationMessages(Player player, PromoCodeData data, String code) {
        Bukkit.getScheduler().runTask(plugin, () -> {
            if (data.getMessages() == null || data.getMessages().isEmpty()) {
                player.sendMessage(messageConfig.getMessage("activation.success"));
            } else {
                for (String message : data.getMessages()) {
                    String processedMessage = message.replace("%code%", code);
                    player.sendMessage(messageConfig.colorize(processedMessage));
                }
            }
        });
    }

    private boolean isPlayerLinked(String playerName) {
        try {
            Class<?> linkAlertClass = Class.forName("org.nezxenka.linkalert.LinkAlert");
            Object databaseManager = linkAlertClass.getField("databaseManager").get(null);
            return (boolean) databaseManager.getClass()
                    .getMethod("isPlayerLinked", String.class)
                    .invoke(databaseManager, playerName.toLowerCase());
        } catch (Exception e) {
            return true;
        }
    }

    public PromoCodeData getPromoCodeData(String code) {
        return configManager.getPromoCode(code);
    }

    public boolean exists(String code) {
        return configManager.isPromoCodeExists(code);
    }

    public int getTotalActivations() {
        return databaseManager.getTotalActivations();
    }

    public int getUniquePlayers() {
        return databaseManager.getUniquePlayers();
    }

    public enum ActivationResult {
        SUCCESS,
        NOT_EXISTS,
        ALREADY_ACTIVATED,
        ALREADY_ACTIVATED_GROUP,
        MAX_USES_REACHED,
        NEED_LINK
    }
}