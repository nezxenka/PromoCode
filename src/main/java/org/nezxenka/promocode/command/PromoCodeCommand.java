package org.nezxenka.promocode.command;

import lombok.RequiredArgsConstructor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.nezxenka.promocode.PromoCode;
import org.nezxenka.promocode.config.MessageConfig;
import org.nezxenka.promocode.model.PromoCodeData;
import org.nezxenka.promocode.service.PromoCodeService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class PromoCodeCommand implements CommandExecutor, TabCompleter {

    private final PromoCode plugin;
    private final PromoCodeService promoCodeService;
    private final MessageConfig messageConfig;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "reload":
                handleReload(sender);
                break;
                
            case "info":
                handleInfo(sender, args);
                break;
                
            case "stats":
                handleStats(sender);
                break;
                
            case "help":
                sendHelp(sender);
                break;
                
            default:
                sender.sendMessage(messageConfig.getMessage("general.unknown-command"));
                break;
        }

        return true;
    }

    private void handleReload(CommandSender sender) {
        if (!sender.hasPermission("promocode.admin")) {
            sender.sendMessage(messageConfig.getNoPermission());
            return;
        }

        try {
            plugin.reload();
            sender.sendMessage(messageConfig.getReloadSuccess());
        } catch (Exception e) {
            sender.sendMessage(messageConfig.getMessage("general.reload-error"));
            plugin.getLogger().severe("Error reloading plugin: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleInfo(CommandSender sender, String[] args) {
        if (!sender.hasPermission("promocode.admin")) {
            sender.sendMessage(messageConfig.getNoPermission());
            return;
        }

        if (args.length < 2) {
            sender.sendMessage(messageConfig.colorize("&cИспользование: /promocode info <код>"));
            return;
        }

        String code = args[1].toLowerCase();
        PromoCodeData data = promoCodeService.getPromoCodeData(code);

        if (data == null) {
            sender.sendMessage(messageConfig.getMessage("commands.info.not-found"));
            return;
        }

        messageConfig.getMessageList("commands.info.header").forEach(line -> 
            sender.sendMessage(line.replace("%code%", code))
        );
        
        sender.sendMessage(messageConfig.getMessage("commands.info.group")
                .replace("%group%", data.getGroup()));
        
        String playerUses = data.getPlayerUses() == -1 ? "Неограничено" : String.valueOf(data.getPlayerUses());
        sender.sendMessage(messageConfig.getMessage("commands.info.player-uses")
                .replace("%uses%", playerUses));
        
        String globalUses = data.getGlobalUses() == -1 ? "Неограничено" : String.valueOf(data.getGlobalUses());
        sender.sendMessage(messageConfig.getMessage("commands.info.global-uses")
                .replace("%uses%", globalUses));
        
        String needLink = data.isNeedLink() ? "Да" : "Нет";
        sender.sendMessage(messageConfig.getMessage("commands.info.need-link")
                .replace("%status%", needLink));
        
        sender.sendMessage(messageConfig.getMessage("commands.info.footer"));
    }

    private void handleStats(CommandSender sender) {
        if (!sender.hasPermission("promocode.admin")) {
            sender.sendMessage(messageConfig.getNoPermission());
            return;
        }

        messageConfig.getMessageList("commands.stats.header").forEach(sender::sendMessage);
        
        int totalActivations = promoCodeService.getTotalActivations();
        sender.sendMessage(messageConfig.getMessage("commands.stats.total-activations")
                .replace("%count%", String.valueOf(totalActivations)));
        
        int uniquePlayers = promoCodeService.getUniquePlayers();
        sender.sendMessage(messageConfig.getMessage("commands.stats.unique-players")
                .replace("%count%", String.valueOf(uniquePlayers)));
        
        sender.sendMessage(messageConfig.getMessage("commands.stats.footer"));
    }

    private void sendHelp(CommandSender sender) {
        messageConfig.getMessageList("commands.help").forEach(sender::sendMessage);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            List<String> subCommands = Arrays.asList("reload", "info", "stats", "help");
            return subCommands.stream()
                    .filter(sub -> sub.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("info")) {
            return new ArrayList<>();
        }

        return completions;
    }
}
