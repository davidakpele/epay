package com.epay.domain.wallet.input;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SwapRequest {

    /** Must be true — user has explicitly accepted the quoted rate. */
    @NotNull(message = "You must accept the exchange rate to proceed")
    private Boolean acceptRate;

    /** Source currency (the wallet being debited). e.g. "NGN" */
    @NotBlank(message = "Source currency is required")
    private String currency;

    /** Target currency (the wallet being credited). e.g. "USD" */
    @NotBlank(message = "Target currency is required")
    private String targetWallet;

    /** Amount in source currency to swap. */
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    private BigDecimal amount;

    @NotNull(message = "User ID is required")
    private Long userId;

    /** 4-digit wallet PIN. */
    @NotBlank(message = "Transaction PIN is required")
    @Size(min = 4, max = 4, message = "Transaction PIN must be exactly 4 digits")
    private String transactionPin;

    /** Client-supplied idempotency key to prevent duplicate swaps. */
    @NotBlank(message = "Idempotency key is required")
    private String idempotencyKey;
}
