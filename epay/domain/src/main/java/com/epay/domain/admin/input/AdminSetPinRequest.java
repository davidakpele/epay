package com.epay.domain.admin.input;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AdminSetPinRequest {

    @NotBlank(message = "PIN is required")
    @Pattern(regexp = "\\d{4}", message = "PIN must be exactly 4 digits")
    private String pin;

    @NotBlank(message = "Reason is required")
    @Size(max = 500)
    private String reason;
}
