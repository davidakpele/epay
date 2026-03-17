package com.example.admin_api_service.payloads;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import com.example.admin_api_service.enums.Currency;
import com.example.admin_api_service.enums.WalletStatus;

/**
 * Represents the system wallet state.
 */
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
    LocalDateTime lastFundedAt
) {}