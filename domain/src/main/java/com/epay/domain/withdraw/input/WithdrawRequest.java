package com.epay.domain.withdraw.input;

import com.epay.domain.withdraw.enums.WithdrawalType;
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
public class WithdrawRequest {

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    @Digits(integer = 15, fraction = 2)
    private BigDecimal amount;

    @NotBlank(message = "Currency is required")
    @Pattern(regexp = "^[A-Z]{3,10}$", message = "Invalid currency code")
    private String currency;

    @NotNull(message = "Withdrawal type is required")
    private WithdrawalType withdrawalType;

    private String bankCode;
    private String accountNumber;
    private String accountName;

    @NotBlank(message = "Transaction PIN is required")
    private String transactionPin;

    @NotBlank(message = "Idempotency key is required")
    private String idempotencyKey;

    private String narration;
    private String ipAddress;
    private String deviceId;
    private String userAgent;
    private Long userId;
}
