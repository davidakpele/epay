package com.epay.common.interfaces;

/**
 * Port interface defined in common.
 * epay-wallet implements this via WalletServiceAdapter.
 * auth calls this — no direct wallet dependency needed.
 */
public interface IWalletPort {

    /**
     * Creates a wallet with the platform default currency for a newly registered user.
     *
     * @param userId          the new user's ID
     * @param defaultCurrency ISO 4217 code e.g. "NGN"
     */
    void createWalletForUser(Long userId, String defaultCurrency);
}
