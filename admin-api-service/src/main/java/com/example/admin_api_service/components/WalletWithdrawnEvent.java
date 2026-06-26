package com.example.admin_api_service.components;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import com.example.admin_api_service.models.SystemWallet;

public record WalletWithdrawnEvent(
        SystemWallet wallet,
        BigDecimal amount,
        Long adminId,
        LocalDateTime occurredAt
) {
    public WalletWithdrawnEvent(SystemWallet wallet, BigDecimal amount, Long adminId) {
        this(wallet, amount, adminId, LocalDateTime.now());
    }
}