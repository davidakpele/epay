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
public class DeleteAccountRequest {

    @NotBlank(message = "Password is required to confirm account deletion")
    private String password;

    @Size(max = 500, message = "Reason must not exceed 500 characters")
    private String reason;

    private String username;
    private Long userId;
    private String email;
    private String action;
}
