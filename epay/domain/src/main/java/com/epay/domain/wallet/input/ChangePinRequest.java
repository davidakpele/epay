package com.epay.domain.wallet.input;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChangePinRequest {

    @NotBlank(message = "Current PIN is required")
    private String currentPin;

    @NotBlank(message = "New PIN is required")
    @Size(min = 4, max = 6, message = "PIN must be 4-6 digits")
    @Pattern(regexp = "^[0-9]+$", message = "PIN must contain digits only")
    private String newPin;

    @NotBlank(message = "Confirm new PIN is required")
    private String confirmNewPin;
}
