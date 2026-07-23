package com.epay.common.interfaces;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Port for writing transaction records from any module (wallet, deposit, withdraw).
 * Implemented by GeneralHistoryAdapter in epay-history.
 *
 * Internally creates one Transaction row per payment with a JSON status timeline.
 * A TransactionAuditLog entry is appended for every call.
 */
public interface IHistoryPort {

    /**
     * Records a completed transaction.
     *
     * @param transactionType DEPOSIT, WITHDRAWAL, TRANSFER_DEBIT, TRANSFER_CREDIT, SWAP, FEE, etc.
     * @param debitCredit     DEBIT or CREDIT
     * @param status          SUCCESS / FAILED / PENDING — maps to TransactionStatus internally
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

    /**
     * Advances an existing transaction to a new status.
     * Creates an audit log entry for the status change.
     */
    void advanceStatus(String transactionId, String newStatus,
                       String actor, String message,
                       String ipAddress, String deviceId, String reason);
}
