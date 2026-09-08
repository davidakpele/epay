package com.epay.domain.auth.dto;

import com.epay.domain.auth.enums.AccountType;
import com.epay.domain.auth.enums.KycDocumentType;
import com.epay.domain.auth.enums.KycStatus;
import com.epay.domain.auth.enums.KycTier;
import com.epay.domain.auth.enums.Role;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;


@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FullUserProfileDTO {

    private Long    id;
    private String  email;
    private String  username;
    private Role    role;
    private AccountType accountType;
    private KycTier kycTier;
    private KycStatus kycStatus;
    private boolean enabled;
    private boolean emailVerified;
    private boolean phoneVerified;
    private boolean twoFactorEnabled;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private PersonalInfo personal;
    private NextOfKinInfo nextOfKin;

    private List<KycDocumentInfo> kycDocuments;

    private AccountSettingsInfo settings;

    @Data
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class PersonalInfo {
        private String    firstName;
        private String    lastName;
        private String    fullName;
        private String    phoneNumber;
        private String    gender;
        private LocalDate dateOfBirth;
        private String    address;
        private String    city;
        private String    state;
        private String    country;
        private String    countryCode;
        private String    referralCode;
        private String    referredByCode;
        private Integer   totalReferrals;
        private String    profilePhotoUrl;
        private boolean   profileComplete;
    }

    @Data
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class NextOfKinInfo {
        private String firstName;
        private String lastName;
        private String relationship;
        private String phone;
        private String email;
        private String address;
    }

    @Data
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class KycDocumentInfo {
        private Long            id;
        private KycDocumentType documentType;
        private KycStatus       status;
        private String          storageReference;
        private String          documentNumber;
        private LocalDate       expiryDate;
        private String          issuingCountry;
        private String          rejectionReason;
        private LocalDateTime   uploadedAt;
    }

    @Data
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class AccountSettingsInfo {
        private boolean emailAlert;
        private boolean transactionAlert;
        private boolean loginAlert;
        private boolean smsMessage;
        private boolean marketingNews;
        private boolean biometric;
        private String  preferredLanguage;
        private String  timeZone;
        private String  sessionTimeout;
    }
}
