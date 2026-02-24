package com.pesco.wallet_service.util;


import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SwapCalculation {
    private BigDecimal baseConvertedAmount;
    private BigDecimal finalAmount;
    private BigDecimal feeAmount;
    private BigDecimal feePercentage;
    private BigDecimal exchangeRate;
}
