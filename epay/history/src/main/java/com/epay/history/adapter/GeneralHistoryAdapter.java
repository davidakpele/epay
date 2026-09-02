package com.epay.history.adapter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import com.epay.common.interfaces.IHistoryPort;
import com.epay.common.interfaces.IHistoryReadPort;
import com.epay.domain.history.dto.TransactionDTO;
import com.epay.domain.history.enums.TransactionStatus;
import com.epay.history.service.HistoryService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class GeneralHistoryAdapter implements IHistoryPort, IHistoryReadPort {

    private final HistoryService historyService;

    // ── IHistoryPort (write) ──────────────────────────────────────────────────

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
        try {
            historyService.record(HistoryService.RecordRequest.of(
                    userId, walletId, transactionId, reference,
                    transactionType, debitCredit, channel, status,
                    grossAmount, feeAmount, netAmount,
                    previousBalance, newBalance, currency, currencySymbol,
                    accountHolder, description,
                    counterpartyAccountHolder, counterpartyUserId, counterpartyWalletId,
                    ipAddress, deviceId, userAgent, adminNote, completedAt));
        } catch (Exception e) {
            log.error("[GeneralHistoryAdapter] Failed to record txn={} type={}: {}",
                    transactionId, transactionType, e.getMessage());
        }
    }

    @Override
    public void advanceStatus(String transactionId, String newStatus,
                               String actor, String message,
                               String ipAddress, String deviceId, String reason) {
        try {
            TransactionStatus status = TransactionStatus.valueOf(newStatus.toUpperCase());
            historyService.advanceStatus(transactionId, status, actor, message,
                    ipAddress, deviceId, reason);
        } catch (Exception e) {
            log.error("[GeneralHistoryAdapter] Failed to advance status for txn={}: {}",
                    transactionId, e.getMessage());
        }
    }

    // ── IHistoryReadPort (read) ───────────────────────────────────────────────

    /**
     * Returns the most recent {@code limit} transactions for a user,
     * ordered newest first. Used by UserTransactionsAgent for fraud detection.
     */
    @Override
    public List<TransactionDTO> findRecentByUserId(Long userId, int limit) {
        try {
            PageRequest pageable = PageRequest.of(0, limit, Sort.by("createdAt").descending());
            return historyService.getByUserId(userId, pageable).getContent();
        } catch (Exception e) {
            log.error("[GeneralHistoryAdapter] Failed to read recent history for userId={}: {}",
                    userId, e.getMessage());
            return List.of();
        }
    }
}
