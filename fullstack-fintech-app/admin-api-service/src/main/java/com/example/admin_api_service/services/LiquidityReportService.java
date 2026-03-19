package com.example.admin_api_service.services;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.admin_api_service.enums.AlertStatus;
import com.example.admin_api_service.enums.Currency;
import com.example.admin_api_service.enums.LiquidityTransactionType;
import com.example.admin_api_service.exceptions.WalletNotFoundException;
import com.example.admin_api_service.models.LiquidityTransaction;
import com.example.admin_api_service.models.SystemWallet;
import com.example.admin_api_service.payloads.LiquidityStatsResponse;
import com.example.admin_api_service.repository.LiquidityThresholdAlertRepository;
import com.example.admin_api_service.repository.LiquidityTransactionRepository;
import com.example.admin_api_service.repository.SystemWalletRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class LiquidityReportService {

    private final SystemWalletRepository walletRepository;
    private final LiquidityTransactionRepository transactionRepository;
    private final LiquidityThresholdAlertRepository alertRepository;

    public LiquidityStatsResponse getStats(Currency currency) {
        SystemWallet wallet = walletRepository.findByCurrency(currency)
                .orElseThrow(() -> new WalletNotFoundException(currency));

        List<LiquidityTransaction> txns = transactionRepository.findBySystemWalletId(wallet.getId());

        BigDecimal totalFunded = txns.stream()
                .filter(t -> t.getType() == LiquidityTransactionType.FUND)
                .map(LiquidityTransaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalWithdrawn = txns.stream()
                .filter(t -> t.getType() == LiquidityTransactionType.WITHDRAWAL)
                .map(LiquidityTransaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long activeAlerts = alertRepository
                .findBySystemWalletIdAndStatus(wallet.getId(), AlertStatus.ACTIVE).size();

        return new LiquidityStatsResponse(
                currency,
                wallet.getBalance(),
                wallet.getAvailableBalance(),
                wallet.getReservedBalance(),
                wallet.getTotalUserLiabilities(),
                wallet.getReserveRatio(),
                wallet.isBelowThreshold(),
                activeAlerts,
                totalFunded,
                totalWithdrawn
        );
    }

    public List<LiquidityStatsResponse> getAllStats() {
        return walletRepository.findAll().stream()
                .map(w -> getStats(w.getCurrency()))
                .toList();
    }
}
