package com.epay.common.interfaces;

import java.math.BigDecimal;

/**
 * Port interface allowing deposit module to credit wallets
 * without a direct dependency on epay-wallet.
 * Implemented by WalletDepositAdapter in epay-wallet.
 */
public interface IDepositWalletPort {

    /**
     * Credits a user's wallet after a successful deposit.
     *
     * @param userId    the wallet owner
     * @param currency  ISO 4217 currency code
     * @param amount    amount to credit
     * @param reference the deposit reference (for idempotency)
     */
    void creditWallet(Long userId, String currency, BigDecimal amount, String reference);

    /**
     * Returns the walletId for a given user (needed to record deposit).
     */
    Long getWalletId(Long userId);
}
