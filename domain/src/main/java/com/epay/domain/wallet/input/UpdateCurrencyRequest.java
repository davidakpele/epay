package com.epay.domain.wallet.input;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Admin request to update an existing supported currency.
 * All fields are optional — only provided fields are updated.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCurrencyRequest {

    @Size(max = 100)
    private String name;

    @Size(max = 10)
    private String symbol;

    @DecimalMin(value = "0.0", inclusive = false, message = "Exchange rate must be greater than zero")
    @Digits(integer = 12, fraction = 8)
    private BigDecimal exchangeRate;

    @Min(0) @Max(8)
    private Integer decimalPlaces;

    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal minDeposit;

    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal minWithdrawal;

    private Boolean active;

    private Boolean defaultEligible;

    @Pattern(regexp = "^[A-Za-z]{2,3}$")
    private String countryCode;
}
