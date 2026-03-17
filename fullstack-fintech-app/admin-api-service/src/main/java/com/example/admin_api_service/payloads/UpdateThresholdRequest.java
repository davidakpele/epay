package com.example.admin_api_service.payloads;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import org.springframework.validation.annotation.Validated;
import com.example.admin_api_service.enums.Currency;

@Validated
public record UpdateThresholdRequest(

        @NotNull(message = "Currency is required")
        Currency currency,
        @NotNull(message = "Minimum threshold is required")
        @DecimalMin(value = "0.00", inclusive = true, message = "Minimum threshold cannot be negative")
        @Digits(integer = 15, fraction = 4, message = "Threshold must not exceed 15 integer digits and 4 decimal places")
        BigDecimal minimumThreshold
) {}
