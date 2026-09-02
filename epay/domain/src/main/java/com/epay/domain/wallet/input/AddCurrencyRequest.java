package com.epay.domain.wallet.input;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddCurrencyRequest {

    @NotBlank(message = "Currency code is required")
    @Pattern(regexp = "^[A-Z]{3,10}$", message = "Invalid currency code")
    private String currencyCode;
}
