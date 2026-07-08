package com.epay.history.adapter;

import com.epay.common.interfaces.IDepositHistoryPort;
import com.epay.history.service.HistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class DepositHistoryAdapter implements IDepositHistoryPort {

    private final HistoryService historyService;

    @Override
    public void recordDepositInitiated(Long userId, String reference, BigDecimal amount,
                                        String currency, String channel) {
        historyService.recordDepositInitiated(userId, reference, amount, currency, channel);
    }

    @Override
    public void recordDepositCompleted(Long userId, Long walletId,
                                        String reference, String gatewayReference,
                                        String transactionId, String channel,
                                        BigDecimal grossAmount, BigDecimal feeAmount,
                                        BigDecimal netAmount, BigDecimal previousBalance,
                                        BigDecimal newBalance, String currency,
                                        String currencySymbol, String accountHolder,
                                        String ipAddress, String deviceId,
                                        String userAgent, String geoLocation,
                                        LocalDateTime completedAt) {
        historyService.recordDepositCompleted(
                userId, walletId, reference, gatewayReference,
                transactionId, channel, grossAmount, feeAmount,
                netAmount, previousBalance, newBalance, currency,
                currencySymbol, accountHolder, ipAddress, deviceId,
                userAgent, geoLocation, completedAt);
    }

    @Override
    public void recordDepositFailed(Long userId, String reference, BigDecimal amount,
                                     String currency, String reason) {
        historyService.recordDepositFailed(userId, reference, amount, currency, reason);
    }
}
