package com.epay.domain.bills.input;

import com.epay.domain.bills.enums.BettingProvider;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class BettingRequest {

    @NotNull(message = "userId is required")
    private Long userId;

    @NotNull(message = "provider is required")
    private BettingProvider provider;

    @NotBlank(message = "bettingAccountId is required")
    @Size(min = 3, message = "bettingAccountId must be at least 3 characters")
    private String bettingAccountId;

    @NotNull(message = "amount is required")
    @DecimalMin(value = "100", message = "Minimum betting fund amount is 100")
    private BigDecimal amount;

    @NotBlank(message = "currency is required")
    private String currency = "NGN";
}
