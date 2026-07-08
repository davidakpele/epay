package com.epay.common.interfaces;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface IDepositHistoryPort {
    void recordDepositInitiated(Long userId, String reference, BigDecimal amount,
                                String currency, String channel);
    void recordDepositCompleted(Long userId, Long walletId,
                                String reference, String gatewayReference,
                                String transactionId, String channel,
                                BigDecimal grossAmount, BigDecimal feeAmount,
                                BigDecimal netAmount, BigDecimal previousBalance,
                                BigDecimal newBalance, String currency,
                                String currencySymbol, String accountHolder,
                                String ipAddress, String deviceId,
                                String userAgent, String geoLocation,
                                LocalDateTime completedAt);

    void recordDepositFailed(Long userId, String reference, BigDecimal amount,
                             String currency, String reason);
}
