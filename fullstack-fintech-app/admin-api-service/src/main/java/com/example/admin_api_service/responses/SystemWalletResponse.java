package com.example.admin_api_service.responses;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.example.admin_api_service.enums.Currency;
import com.example.admin_api_service.enums.WalletStatus;
import com.example.admin_api_service.models.SystemWallet;

public record SystemWalletResponse(
        String id,
        Currency currency,
        BigDecimal balance,
        BigDecimal reservedBalance,
        BigDecimal availableBalance,
        BigDecimal minimumThreshold,
        BigDecimal totalUserLiabilities,
        BigDecimal reserveRatio,
        WalletStatus status,
        boolean belowThreshold,
        String walletReference,
        String notes,
        LocalDateTime lastFundedAt,
        Long lastFundedByAdminId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static SystemWalletResponse from(SystemWallet wallet) {
        return new SystemWalletResponse(
                wallet.getId(),
                wallet.getCurrency(),
                wallet.getBalance(),
                wallet.getReservedBalance(),
                wallet.getAvailableBalance(),
                wallet.getMinimumThreshold(),
                wallet.getTotalUserLiabilities(),
                wallet.getReserveRatio(),
                wallet.getStatus(),
                wallet.isBelowThreshold(),
                wallet.getWalletReference(),
                wallet.getNotes(),
                wallet.getLastFundedAt(),
                wallet.getLastFundedByAdminId(),
                wallet.getCreatedAt(),
                wallet.getUpdatedAt()
        );
    }
}
