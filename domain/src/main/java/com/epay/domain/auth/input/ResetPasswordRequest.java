package com.epay.domain.auth.input;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResetPasswordRequest {

    @NotBlank(message = "Identifier is required")
    private String identifier;

    @NotBlank(message = "OTP is required")
    @Size(min = 4, max = 8, message = "Invalid OTP format")
    private String otp;

    @NotBlank(message = "New password is required")
    @Size(min = 8, max = 128, message = "Password must be between 8 and 128 characters")
    private String newPassword;

    @NotBlank(message = "Confirm password is required")
    private String confirmNewPassword;
}
