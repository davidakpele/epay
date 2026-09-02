package com.epay.domain.investment.input;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class TopUpTargetSavingsRequest {

    @NotNull
    private Long userId;

    @NotNull
    private Long walletId;

    @NotNull
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;
}