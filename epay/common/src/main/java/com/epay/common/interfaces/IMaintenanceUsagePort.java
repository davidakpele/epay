package com.epay.common.interfaces;

import java.math.BigDecimal;

/**
 * Port interface through which any module (wallet, deposit, bills, etc.) notifies
 * the maintenance service that a user has actively used a currency this month.
 *
 * Implementations are fire-and-forget and must NEVER throw checked exceptions that
 * would roll back the caller's transaction.  All failures should be caught and logged
 * inside the implementation.
 *
 * The wallet service injects this port and calls {@link #recordActivity} after every
 * successful balance mutation (transfer, deposit credit, swap, bill payment, etc.).
 * The maintenance scheduler later reads the accumulated monthly activity to decide
 * which currencies to charge per user.
 */
public interface IMaintenanceUsagePort {

    /**
     * Records that {@code userId} performed a transaction of {@code amount} in
     * {@code currencyCode} with the given {@code transactionType}.
     *
     * This call is non-blocking.  It should be invoked asynchronously from the caller
     * so that tracking latency never affects the main transaction response time.
     *
     * @param userId          the user who performed the transaction
     * @param currencyCode    ISO-4217 code of the currency used (e.g. "USD")
     * @param amount          gross transaction amount (always positive)
     * @param transactionType e.g. "TRANSFER_DEBIT", "DEPOSIT", "SWAP", "BILL_PAYMENT"
     */
    void recordActivity(Long userId, String currencyCode, BigDecimal amount, String transactionType);

    /**
     * Checks whether the user currently has any outstanding maintenance fee debt in
     * the given currency.  Called by the wallet service before crediting any incoming
     * deposit or transfer so that debt recovery can be triggered first.
     *
     * @return true if an active (ACTIVE or PARTIAL) debt row exists for this user/currency
     */
    boolean hasActiveDebt(Long userId, String currencyCode);

    /**
     * Triggers real-time debt recovery for a user in a specific currency when new
     * funds arrive.  The implementation deducts as much debt as possible from the
     * incoming credit, updates the debt ledger, notifies the user, and returns the
     * remaining net amount that should actually be credited to the wallet.
     *
     * @param userId        the user receiving funds
     * @param currencyCode  the currency being credited
     * @param incomingAmount the gross incoming amount (before debt recovery)
     * @param walletId       the wallet ID being credited (for history recording)
     * @return the net amount to credit after debt was deducted (may equal incomingAmount
     *         if no debt existed, or be lower / zero if debt consumed some/all of it)
     */
    BigDecimal recoverDebtOnCredit(Long userId, String currencyCode, BigDecimal incomingAmount, Long walletId);
}
