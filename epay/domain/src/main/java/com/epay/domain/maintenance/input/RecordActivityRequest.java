package com.epay.domain.maintenance.input;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecordActivityRequest {

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotBlank(message = "Currency code is required")
    private String currencyCode;

    @NotNull(message = "Transaction amount is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Amount must be positive")
    private BigDecimal amount;

    @NotBlank(message = "Transaction type is required")
    private String transactionType;
}
