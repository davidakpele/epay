package com.epay.domain.wallet.input;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateCurrencyRequest {

    @NotBlank(message = "Currency code is required")
    @Size(min = 3, max = 10, message = "Currency code must be between 3 and 10 characters")
    @Pattern(regexp = "^[A-Za-z]+$", message = "Currency code must contain only letters")
    private String code;

    @NotBlank(message = "Currency name is required")
    @Size(max = 100)
    private String name;

    @NotBlank(message = "Currency symbol is required")
    @Size(max = 10)
    private String symbol;

    @NotNull(message = "Exchange rate is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Exchange rate must be greater than zero")
    @Digits(integer = 12, fraction = 8)
    private BigDecimal exchangeRate;

    @Min(value = 0, message = "Decimal places cannot be negative")
    @Max(value = 8, message = "Decimal places cannot exceed 8")
    private int decimalPlaces = 2;

    @DecimalMin(value = "0.0", inclusive = false, message = "Min deposit must be greater than zero")
    private BigDecimal minDeposit;

    @DecimalMin(value = "0.0", inclusive = false, message = "Min withdrawal must be greater than zero")
    private BigDecimal minWithdrawal;

    private boolean defaultEligible;

    @Pattern(regexp = "^[A-Za-z]{2,3}$", message = "Country code must be 2-3 letters")
    private String countryCode;
}
