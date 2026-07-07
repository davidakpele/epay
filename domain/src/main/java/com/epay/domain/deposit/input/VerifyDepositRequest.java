package com.epay.domain.deposit.input;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VerifyDepositRequest {

    @NotBlank(message = "Reference is required")
    private String reference;
}
