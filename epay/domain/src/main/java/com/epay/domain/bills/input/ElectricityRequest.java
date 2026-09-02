package com.epay.domain.bills.input;

import com.epay.domain.bills.enums.ElectricityProvider;
import com.epay.domain.bills.enums.MeterType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ElectricityRequest {

    @NotNull(message = "userId is required")
    private Long userId;

    @NotNull(message = "provider is required")
    private ElectricityProvider provider;

    @NotBlank(message = "meterNumber is required")
    @Pattern(regexp = "\\d{6,}", message = "meterNumber must be numeric and at least 6 digits")
    private String meterNumber;

    @NotNull(message = "meterType is required")
    private MeterType meterType;

    @NotNull(message = "amount is required")
    @DecimalMin(value = "50", message = "Minimum electricity payment is 50")
    private BigDecimal amount;

    @NotBlank(message = "currency is required")
    private String currency = "NGN";
}
