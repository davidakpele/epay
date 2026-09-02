package com.epay.domain.auth.dto;

import com.epay.domain.auth.enums.UserStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserRecordDTO {

    private Long id;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String gender;
    private String countryCode;
    private String country;
    private String city;
    private String state;
    private LocalDate dateOfBirth;
    private String address;
    private UserStatus status;
    private String referralCode;
    private String referredByCode;
    private Integer totalReferrals;
    private String profilePhotoUrl;
    private boolean profileComplete;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
