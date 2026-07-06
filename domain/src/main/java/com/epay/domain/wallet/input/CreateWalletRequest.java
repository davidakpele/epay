package com.epay.domain.wallet.input;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateWalletRequest {

    @NotNull(message = "User ID is required")
    @Positive(message = "User ID must be positive")
    private Long userId;

    /** ISO 4217 default currency for this wallet — e.g. NGN, USD. */
    @NotBlank(message = "Default currency is required")
    @Pattern(regexp = "^[A-Z]{3,10}$", message = "Invalid currency code")
    private String defaultCurrency;
}
