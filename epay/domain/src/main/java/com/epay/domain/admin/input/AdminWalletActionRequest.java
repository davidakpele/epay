package com.epay.domain.admin.input;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AdminWalletActionRequest {
    @NotBlank(message = "Reason is required")
    @Size(max = 500)
    private String reason;
}
