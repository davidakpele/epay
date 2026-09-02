package com.epay.common.constants;

public final class CacheKeys {
    public static final String OTP_PREFIX           = "otp:%s:%s";
    public static final String COOLDOWN_PREFIX      = "cd:%s:%s";
    public static final String BLACKLIST_PREFIX     = "bl:%s:%s";

    public static final String WALLET_BALANCE       = "wallet:balance:%s:%s";

    public static final String USER_PROFILE         = "user:profile:%s";

    public static final String EXCHANGE_RATE        = "fx:%s:%s";

    public static String otp(Long userId, String purpose) {
        return String.format(OTP_PREFIX, userId, purpose);
    }

    public static String cooldown(String action, String identifier) {
        return String.format(COOLDOWN_PREFIX, action, identifier);
    }

    public static String blacklist(String type, String value) {
        return String.format(BLACKLIST_PREFIX, type, value);
    }

    private CacheKeys() {}
}
