package com.epay.common.interfaces;

import java.math.BigDecimal;

/**
 * Port interface for real-time maintenance debt recovery.
 *
 * Implemented by {@code DebtRecoveryService} in the {@code epay-maintenance} module.
 * Consumed by {@code WalletService} in the {@code epay-wallet} module to avoid
 * a circular module dependency (wallet → maintenance → wallet).
 *
 * Both modules depend on {@code epay-common} so the port interface lives here.
 *
 * The implementing bean is only present at runtime if the maintenance module is
 * on the classpath (it always is in the {@code epay-main} assembly).
 * The wallet service checks {@code IMaintenanceUsagePort.hasActiveDebt()} first so
 * this method is only invoked when a real debt exists, keeping the hot path fast.
 */
public interface IDebtRecoveryPort {

    /**
     * Recovers outstanding maintenance fee debt from an incoming credit.
     *
     * Called synchronously inside the wallet credit transaction so both the balance
     * update and the debt recovery commit atomically.
     *
     * @param userId         the user receiving the credit
     * @param currencyCode   the currency being credited
     * @param incomingAmount the gross incoming amount (positive)
     * @param walletId       the wallet ID being credited (for history)
     * @return the net amount to actually credit (incomingAmount − recoveredDebt)
     */
    BigDecimal recoverOnCredit(Long userId, String currencyCode,
                               BigDecimal incomingAmount, Long walletId);
}
