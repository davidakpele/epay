package com.example.admin_api_service.payloads;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import org.springframework.validation.annotation.Validated;
import com.example.admin_api_service.enums.Currency;

/**
 * Request to fund a wallet.
 */
// FundWalletRequest.java
@Validated
public record FundWalletRequest(

        @NotNull(message = "Currency is required")
        Currency currency,

        @NotNull(message = "Amount is required")
        @DecimalMin(value = "0.0001", message = "Amount must be greater than zero")
        @Digits(integer = 15, fraction = 4, message = "Amount must not exceed 15 integer digits and 4 decimal places")
        BigDecimal amount,

        @NotBlank(message = "External reference is required")
        @Size(max = 100, message = "External reference must not exceed 100 characters")
        String externalReference,

        @Size(max = 500, message = "Description must not exceed 500 characters")
        String description

) {}