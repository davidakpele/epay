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
public class TransferRequest {

    @NotNull(message = "Sender user ID is required")
    @Positive
    private Long senderUserId;

    @NotBlank(message = "Recipient username is required")
    private String recipientUsername;

    @NotBlank(message = "Currency is required")
    @Pattern(regexp = "^[A-Z]{3,10}$", message = "Invalid currency code")
    private String currency;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    @Digits(integer = 15, fraction = 8)
    private BigDecimal amount;

    @Size(max = 200)
    private String narration;

    /** Client-generated idempotency key — prevents duplicate transfers on retry. */
    @NotBlank(message = "Idempotency key is required")
    private String idempotencyKey;

    /** Hashed transaction PIN. */
    @NotBlank(message = "Transaction PIN is required")
    private String transactionPin;
}
