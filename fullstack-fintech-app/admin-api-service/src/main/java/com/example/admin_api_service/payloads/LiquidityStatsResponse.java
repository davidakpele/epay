package com.example.admin_api_service.payloads;

import java.math.BigDecimal;
import com.example.admin_api_service.enums.Currency;

/**
 * Aggregated liquidity statistics.
 */
public record LiquidityStatsResponse(
    Currency currency,
    BigDecimal totalBalance,
    BigDecimal availableBalance,
    BigDecimal reservedBalance,
    BigDecimal totalUserLiabilities,
    BigDecimal reserveRatio,
    boolean belowThreshold,
    long activeAlerts,
    BigDecimal totalFunded,
    BigDecimal totalWithdrawn
) {}