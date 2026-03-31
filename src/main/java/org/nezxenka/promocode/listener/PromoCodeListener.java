package org.nezxenka.promocode.listener;

import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.nezxenka.promocode.PromoCode;
import org.nezxenka.promocode.config.MessageConfig;
import org.nezxenka.promocode.service.PromoCodeService;

@RequiredArgsConstructor
public class PromoCodeListener implements Listener {

    private final PromoCode plugin;
    private final PromoCodeService promoCodeService;
    private final MessageConfig messageConfig;

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage();

        String[] args = message.substring(1).split(" ");
        String command = args[0].toLowerCase();

        if (!promoCodeService.exists(command)) {
            return;
        }

        event.setCancelled(true);

        promoCodeService.activatePromoCode(player, command).thenAccept(result -> {
            switch (result) {
                case SUCCESS:
                    break;
                    
                case NOT_EXISTS:
                    player.sendMessage(messageConfig.getNotExists());
                    break;
                    
                case ALREADY_ACTIVATED:
                    player.sendMessage(messageConfig.getAlreadyActivated());
                    break;
                    
                case ALREADY_ACTIVATED_GROUP:
                    player.sendMessage(messageConfig.getAlreadyActivatedGroup());
                    break;
                    
                case MAX_USES_REACHED:
                    player.sendMessage(messageConfig.getMaxUsesReached());
                    break;
                    
                case NEED_LINK:
                    messageConfig.getNeedLinkMessage().forEach(player::sendMessage);
                    break;
            }
        }).exceptionally(throwable -> {
            plugin.getLogger().severe("Error activating promocode: " + throwable.getMessage());
            throwable.printStackTrace();
            player.sendMessage(messageConfig.colorize("&cПроизошла ошибка при активации промокода!"));
            return null;
        });
    }
}
