package org.nezxenka.promocode.util;

import org.bukkit.ChatColor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ColorUtil {

    private static final Pattern HEX_PATTERN = Pattern.compile("&x(&[0-9A-Fa-f]){6}");

    public static String colorize(String message) {
        if (message == null) return "";
        
        // Обработка hex цветов
        Matcher matcher = HEX_PATTERN.matcher(message);
        StringBuffer buffer = new StringBuffer();
        
        while (matcher.find()) {
            String hexCode = matcher.group().replace("&x", "").replace("&", "");
            matcher.appendReplacement(buffer, net.md_5.bungee.api.ChatColor.of("#" + hexCode).toString());
        }
        matcher.appendTail(buffer);
        
        // Обработка обычных цветовых кодов
        return ChatColor.translateAlternateColorCodes('&', buffer.toString());
    }
}
