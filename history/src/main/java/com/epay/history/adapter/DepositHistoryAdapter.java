package com.epay.history.adapter;

import com.epay.common.interfaces.IDepositHistoryPort;
import com.epay.history.service.HistoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class DepositHistoryAdapter implements IDepositHistoryPort {

    private final HistoryService historyService;

    @Override
    public void recordDepositInitiated(Long userId, String reference,
                                        BigDecimal amount, String currency, String channel) {
        try {
            historyService.record(new HistoryService.RecordRequest(
                    userId, null,
                    null, reference,
                    "INIT_" + reference,
                    "DEPOSIT", "CREDIT",
                    channel, "PENDING",
                    amount, BigDecimal.ZERO, amount,
                    null, null,
                    currency, currency,
                    null, "DEPOSIT INITIATED via " + channel,
                    null, null, null, null,
                    null, null, null, null, null));
        } catch (Exception e) {
            log.error("[DepositHistory] Failed to record initiated deposit ref={}: {}",
                    reference, e.getMessage());
        }
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
        try {
            historyService.record(new HistoryService.RecordRequest(
                    userId, walletId,
                    transactionId, reference,
                    "COMP_" + reference,
                    "DEPOSIT", "CREDIT",
                    channel, "SUCCESS",
                    grossAmount, safe(feeAmount), safe(netAmount),
                    previousBalance, newBalance,
                    currency, currencySymbol,
                    accountHolder,
                    "DEPOSIT//INTO " + upper(accountHolder) + " " + currency + " ACCOUNT",
                    null, null, null, null,
                    ipAddress, deviceId, userAgent,
                    "Deposit of " + currency + " " + grossAmount + " via " + channel,
                    completedAt));
        } catch (Exception e) {
            log.error("[DepositHistory] Failed to record completed deposit ref={}: {}",
                    reference, e.getMessage());
        }
    }

    @Override
    public void recordDepositFailed(Long userId, String reference,
                                     BigDecimal amount, String currency, String reason) {
        try {
            historyService.record(new HistoryService.RecordRequest(
                    userId, null,
                    null, reference,
                    "FAIL_" + reference,
                    "DEPOSIT", "CREDIT",
                    null, "FAILED",
                    safe(amount), BigDecimal.ZERO, safe(amount),
                    null, null,
                    currency, currency,
                    null, "DEPOSIT FAILED: " + reason,
                    reason, null, null, null,
                    null, null, null, null, null));
        } catch (Exception e) {
            log.error("[DepositHistory] Failed to record failed deposit ref={}: {}",
                    reference, e.getMessage());
        }
    }

    private BigDecimal safe(BigDecimal v) { return v != null ? v : BigDecimal.ZERO; }
    private String upper(String v) { return v != null ? v.toUpperCase() : ""; }
}
