package com.epay.domain.admin.input;

import com.epay.domain.auth.enums.AccountType;
import com.epay.domain.auth.enums.KycStatus;
import com.epay.domain.auth.enums.KycTier;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AdminUpdateUserRequest {

    @Size(max = 100)
    private String firstName;

    @Size(max = 100)
    private String lastName;

    @Email
    private String email;

    private String phone;
    private String gender;
    private String dateOfBirth;
    private String address;
    private String city;
    private String state;
    private String country;
    private String countryCode;

    private AccountType accountType;
    private KycTier     kycTier;
    private KycStatus   kycStatus;
    private Boolean emailVerified;
    private Boolean phoneVerified;
    @Size(max = 500)
    private String adminNote;
}
