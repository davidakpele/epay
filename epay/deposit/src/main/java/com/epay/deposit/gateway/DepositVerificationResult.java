package com.epay.deposit.gateway;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class DepositVerificationResult {
    private boolean     success;
    private String      gatewayReference;
    private BigDecimal  amount;
    private String      currency;
    private String      failureReason;
}
