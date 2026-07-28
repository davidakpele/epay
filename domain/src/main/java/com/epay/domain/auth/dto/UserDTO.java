package com.epay.domain.auth.dto;

import com.epay.domain.auth.enums.AccountType;
import com.epay.domain.auth.enums.KycStatus;
import com.epay.domain.auth.enums.KycTier;
import com.epay.domain.auth.enums.Role;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserDTO {

    private Long id;
    private String email;
    private String username;
    private Role role;
    private AccountType accountType;
    private KycTier kycTier;
    private KycStatus kycStatus;
    private boolean enabled;
    private boolean emailVerified;
    private boolean phoneVerified;
    private boolean twoFactorAuth;
    private LocalDateTime createdOn;
    private LocalDateTime updatedOn;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private UserRecordDTO profile;
}
