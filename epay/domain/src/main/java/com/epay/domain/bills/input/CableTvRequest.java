package com.epay.domain.bills.input;

import com.epay.domain.bills.enums.CableTvProvider;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CableTvRequest {

    @NotNull(message = "userId is required")
    private Long userId;

    @NotNull(message = "provider is required")
    private CableTvProvider provider;

    @NotBlank(message = "smartCardNumber is required")
    @Pattern(regexp = "\\d{6,}", message = "smartCardNumber must be numeric and at least 6 digits")
    private String smartCardNumber;

    @NotBlank(message = "planName is required")
    private String planName;

    @NotBlank(message = "planCode is required")
    private String planCode;

    @NotNull(message = "amount is required")
    @DecimalMin(value = "0.01", message = "amount must be greater than 0")
    private BigDecimal amount;

    @NotBlank(message = "currency is required")
    private String currency = "NGN";
}
