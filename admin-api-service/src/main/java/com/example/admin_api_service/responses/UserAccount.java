package com.example.admin_api_service.responses;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserAccount {
    private Long id;
    private String email;
    private String username;
    private Instant createdOn;
    private Instant updatedOn;
    private Boolean enabled;
    private Boolean twoFactorAuth;
    private List<Record> records;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Record {
        private Long id;
        private String firstName;
        private String lastName;
        private String telephone;
        private String gender;
        private String country;
        private String city;
        private String nextOfKing;
        private LocalDate dateofBirth;
        private String address;
        private Boolean isTransferPinSet;
        private Boolean locked;
        private Instant lockedAt;
        private Boolean isBlocked;
        private Long blockedDuration;
        private Instant blockedUntil;
        private String blockedReason;
        private String referralCode;
        private Integer totalReferers;
        private String referralLink;
        private String photo;
    }
}
