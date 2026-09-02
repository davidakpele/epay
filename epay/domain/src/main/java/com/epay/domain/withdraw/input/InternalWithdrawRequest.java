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
public class InternalWithdrawRequest {

    @NotBlank(message = "Username is required")
    private String username;

    @NotNull(message = "User Id is required")
    private Long userId;

    @NotNull(message = "Amount is required")
    private BigDecimal amount;

    @NotBlank(message = "Currency is required")
    @Pattern(regexp = "^[A-Z]{3,10}$", message = "Invalid currency code")
    private String currency;

    @NotNull(message = "Withdrawal type is required")
    private WithdrawalType withdrawalType;

    @NotBlank(message = "Transfer Pin is required")
    @Size(min = 4, max = 4, message = "Your Transfer Pin must be 4 Digit")
    private String transferPin;

    @NotBlank(message = "Idempotency key is required")
    private String idempotencyKey;
    
    @NotBlank(message = "Recipient is required")
    @NotNull(message = "Recipient is required")
    private String recipient;

    private String narration;

}
