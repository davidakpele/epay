package com.epay.common.interfaces;

public interface IWalletPort {

    /**
     * Creates a wallet for a new user with ALL supported currencies.
     * The defaultCurrency is marked as the primary/default balance.
     *
     * @param userId          the new user's ID
     * @param defaultCurrency ISO 4217 code e.g. "NGN" — used as the default balance
     */
    void createWalletForUser(Long userId, String defaultCurrency);
}
