package com.epay.domain.auth.input;

import com.epay.domain.auth.enums.TokenPurpose;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OtpVerificationRequest {

    @NotBlank(message = "OTP is required")
    @Size(min = 4, max = 8, message = "Invalid OTP format")
    private String otp;

    @NotNull(message = "Purpose is required")
    private TokenPurpose purpose;
}
