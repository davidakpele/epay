package com.epay.common.interfaces;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface IHistoryPort {


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

 
    void advanceStatus(String transactionId, String newStatus,
                       String actor, String message,
                       String ipAddress, String deviceId, String reason);
}
