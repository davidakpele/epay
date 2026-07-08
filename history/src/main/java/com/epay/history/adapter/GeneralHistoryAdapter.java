package com.epay.history.adapter;

import com.epay.common.interfaces.IHistoryPort;
import com.epay.history.service.HistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.time.LocalDateTime;


@Component
@RequiredArgsConstructor
public class GeneralHistoryAdapter implements IHistoryPort {

    private final HistoryService historyService;

    @Override
    public void record(Long userId, Long walletId,
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
                        String adminNote, LocalDateTime completedAt) {

        historyService.record(
                userId, walletId, transactionId, reference,
                transactionType, debitCredit, channel, status,
                grossAmount, feeAmount, netAmount,
                previousBalance, newBalance, currency, currencySymbol,
                accountHolder, description,
                counterpartyAccountHolder, counterpartyUserId, counterpartyWalletId,
                ipAddress, deviceId, userAgent, adminNote, completedAt);
    }
}
