package org.nezxenka.promocode.util;

import java.util.regex.Pattern;

public class ValidationUtil {

    private static final Pattern PROMO_CODE_PATTERN = Pattern.compile("^[a-zA-Z0-9_-]{3,32}$");
    private static final Pattern IP_PATTERN = Pattern.compile(
            "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$"
    );

    public static boolean isValidPromoCode(String code) {
        return code != null && PROMO_CODE_PATTERN.matcher(code).matches();
    }

    public static boolean isValidIP(String ip) {
        return ip != null && IP_PATTERN.matcher(ip).matches();
    }

    public static boolean isValidPlayerName(String name) {
        return name != null && name.length() >= 3 && name.length() <= 16;
    }
}