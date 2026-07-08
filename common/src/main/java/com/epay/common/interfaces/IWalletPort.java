package com.epay.common.interfaces;

public interface IWalletPort {

    /**
     * @param userId          the new user's ID
     * @param defaultCurrency ISO 4217 code e.g. "NGN"
     */
    void createWalletForUser(Long userId, String defaultCurrency);
}
