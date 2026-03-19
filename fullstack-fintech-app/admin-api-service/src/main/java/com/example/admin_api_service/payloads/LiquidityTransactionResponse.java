package com.example.admin_api_service.payloads;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import com.example.admin_api_service.enums.LiquidityTransactionType;
import com.example.admin_api_service.enums.TransactionStatus;
import jakarta.annotation.Nullable;

/**
 * Represents a liquidity transaction.
 */
public record LiquidityTransactionResponse(
    String id,
    LiquidityTransactionType type,
    BigDecimal amount,
    BigDecimal balanceBefore,
    BigDecimal balanceAfter,
    String reference,
    String externalReference,
    TransactionStatus status,
    @Nullable String description,
    LocalDateTime createdAt
) {}