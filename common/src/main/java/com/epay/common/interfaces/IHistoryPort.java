package com.epay.common.interfaces;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * General-purpose history port for all transaction types.
 * All modules use this to write audit records without importing epay-history.
 * Implemented by GeneralHistoryAdapter in epay-history.
 */
public interface IHistoryPort {

    /**
     * Generic method — covers transfer, swap, withdrawal, fee, refund, reversal.
     *
     * @param transactionType   DEPOSIT, WITHDRAWAL, TRANSFER_DEBIT, TRANSFER_CREDIT,
     *                          SWAP, FEE, REFUND, REVERSAL, SAVINGS_DEBIT, etc.
     * @param debitCredit       "DEBIT" or "CREDIT"
     * @param status            SUCCESS, FAILED, PENDING
     */
    void record(Long userId, Long walletId,
                String transactionId, String reference,
                String transactionType, String debitCredit,
                String channel, String status,
                BigDecimal grossAmount, BigDecimal feeAmount,
                BigDecimal netAmount, BigDecimal previousBalance,
                BigDecimal newBalance, String currency, String currencySymbol,
                String accountHolder, String description,
                String counterpartyAccountHolder, Long counterpartyUserId,
                Long counterpartyWalletId, String ipAddress,
                String deviceId, String userAgent,
                String adminNote, LocalDateTime completedAt);
}
