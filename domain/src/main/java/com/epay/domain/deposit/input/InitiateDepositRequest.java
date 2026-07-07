package com.epay.domain.deposit.input;

import com.epay.domain.deposit.enums.DepositChannel;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InitiateDepositRequest {

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    @Digits(integer = 15, fraction = 2)
    private BigDecimal amount;

    @NotBlank(message = "Currency is required")
    @Pattern(regexp = "^[A-Z]{3,10}$", message = "Invalid currency code")
    private String currency;

    @NotNull(message = "Channel is required")
    private DepositChannel channel;

    /** Optional callback URL for redirect after payment. */
    private String callbackUrl;

    /** Client-supplied idempotency key — prevents duplicate initiations. */
    @NotBlank(message = "Idempotency key is required")
    private String idempotencyKey;
}
