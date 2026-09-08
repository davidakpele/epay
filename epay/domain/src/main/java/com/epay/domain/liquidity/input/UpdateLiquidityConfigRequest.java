package com.epay.domain.liquidity.input;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class UpdateLiquidityConfigRequest {

    @NotNull
    @DecimalMin("0.00")
    private BigDecimal alertThreshold;

    @NotNull
    @DecimalMin("0.00")
    private BigDecimal blockThreshold;

    private String alertEmails;   

    private Boolean alertsEnabled;
}
