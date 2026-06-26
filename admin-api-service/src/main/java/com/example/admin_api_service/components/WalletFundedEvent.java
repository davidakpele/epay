package com.example.admin_api_service.components;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import com.example.admin_api_service.models.SystemWallet;

// WalletFundedEvent.java
public record WalletFundedEvent(
        SystemWallet wallet,
        BigDecimal amount,
        Long adminId,
        LocalDateTime occurredAt
) {
    public WalletFundedEvent(SystemWallet wallet, BigDecimal amount, Long adminId) {
        this(wallet, amount, adminId, LocalDateTime.now());
    }
}