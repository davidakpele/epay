package com.example.admin_api_service.payloads;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import org.springframework.validation.annotation.Validated;
import com.example.admin_api_service.enums.Currency;

@Validated
public record RebalanceRequest(

        @NotNull(message = "Source currency is required")
        Currency fromCurrency,

        @NotNull(message = "Destination currency is required")
        Currency toCurrency,

        @NotNull(message = "Amount is required")
        @DecimalMin(value = "0.0001", message = "Amount must be greater than zero")
        @Digits(integer = 15, fraction = 4, message = "Amount must not exceed 15 integer digits and 4 decimal places")
        BigDecimal amount,

        @Size(max = 500, message = "Description must not exceed 500 characters")
        String description

) {
    public RebalanceRequest {
        if (fromCurrency.equals(toCurrency)) {
            throw new IllegalArgumentException("Source and destination currencies must be different");
        }
    }
}