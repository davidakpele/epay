package com.epay.domain.auth.input;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Partial-update request for user profile.
 * All fields are optional — only non-null values are applied.
 *
 * Frontend payload:
 *   { firstName, lastName, email, gender, address,
 *     dob, telephone, country, state, city }
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProfileRequest {

    @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
    private String firstName;

    @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
    private String lastName;

    /** Optional — updates the user's login email. */
    @Email(message = "Please enter a valid email address")
    private String email;

    /** Maps to UserRecord.phoneNumber */
    @Pattern(regexp = "^\\+?[1-9]\\d{6,14}$", message = "Please enter a valid phone number with country code")
    private String telephone;

    /** Date of birth — frontend sends ISO date string e.g. "2026-08-06" */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate dob;

    @Pattern(regexp = "^(male|female|other|prefer_not_to_say)$",
             flags = Pattern.Flag.CASE_INSENSITIVE,
             message = "Gender must be male, female, other, or prefer_not_to_say")
    private String gender;

    @Size(max = 500, message = "Address must not exceed 500 characters")
    private String address;

    @Size(max = 100)
    private String city;

    @Size(max = 100)
    private String state;

    @Size(max = 100)
    private String country;

    @Pattern(regexp = "^[A-Z]{2,3}$", message = "Country code must be a 2-3 letter ISO code")
    private String countryCode;
}
