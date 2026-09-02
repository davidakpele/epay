package com.epay.domain.admin.input;

import com.epay.domain.auth.enums.AccountType;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class AdminCreateUserRequest {

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50)
    private String username;

    @NotBlank(message = "Email is required")
    @Email
    private String email;

    private String phone;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    private AccountType accountType = AccountType.INDIVIDUAL;

    private boolean createWallet = true;
    private String defaultCurrency = "NGN";
}
