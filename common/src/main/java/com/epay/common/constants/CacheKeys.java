package com.epay.common.constants;

public final class CacheKeys {

    // OTP cache — keyed by userId:purpose
    public static final String OTP_PREFIX           = "otp:%s:%s";

    // Rate limit / cool-down — keyed by action:identifier
    public static final String COOLDOWN_PREFIX      = "cd:%s:%s";

    // Blacklist — keyed by type:value
    public static final String BLACKLIST_PREFIX     = "bl:%s:%s";

    // Wallet balance cache — keyed by walletId:currency
    public static final String WALLET_BALANCE       = "wallet:balance:%s:%s";

    // User profile cache — keyed by userId
    public static final String USER_PROFILE         = "user:profile:%s";

    // Exchange rate cache — keyed by fromCurrency:toCurrency
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
