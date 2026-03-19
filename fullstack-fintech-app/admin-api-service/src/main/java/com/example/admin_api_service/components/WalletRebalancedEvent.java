package com.example.admin_api_service.components;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import com.example.admin_api_service.models.SystemWallet;

public record WalletRebalancedEvent(
        SystemWallet fromWallet,
        SystemWallet toWallet,
        BigDecimal amount,
        Long adminId,
        LocalDateTime occurredAt
) {
    public WalletRebalancedEvent(SystemWallet fromWallet, SystemWallet toWallet, BigDecimal amount, Long adminId) {
        this(fromWallet, toWallet, amount, adminId, LocalDateTime.now());
    }
}