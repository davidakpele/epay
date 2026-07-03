package com.epay.domain.auth.input;

import com.epay.domain.auth.enums.ContactMethod;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ForgotPasswordRequest {

    @NotBlank(message = "Identifier (email or phone) is required")
    private String identifier;

    @NotNull(message = "Delivery channel is required")
    private ContactMethod channel;
}
