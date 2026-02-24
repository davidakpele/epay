package com.pesco.wallet_service.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CurrencySwapResponse {
    private String transactionId;
    private BigDecimal fromAmount;
    private BigDecimal toAmount;
    private BigDecimal exchangeRate;
    private BigDecimal feeAmount;
    private BigDecimal feePercentage;
    private String status;
    private LocalDateTime timestamp;
}
