package com.epay.domain.investment.input;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

import com.epay.domain.investment.enums.InvestmentDuration;

@Data
public class CreateInvestmentRequest {

    @NotNull
    private Long userId;

    @NotNull
    private Long walletId;

    @NotNull
    @Size(max = 10)
    private String currencyCode = "NGN";

    @NotNull
    @DecimalMin(value = "0.01", message = "Principal must be greater than 0")
    private BigDecimal principal;

    @NotNull
    private InvestmentDuration duration;
}