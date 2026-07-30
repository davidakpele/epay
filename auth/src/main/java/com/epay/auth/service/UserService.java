package com.epay.auth.service;

import com.epay.domain.auth.dto.FullUserProfileDTO;
import com.epay.domain.auth.entity.KycDocument;
import com.epay.domain.auth.entity.NextOfKin;
import com.epay.domain.auth.entity.User;
import com.epay.domain.auth.entity.UserAccountSettings;
import com.epay.domain.auth.entity.UserRecord;
import com.epay.domain.auth.repository.KycDocumentRepository;
import com.epay.domain.auth.repository.NextOfKinRepository;
import com.epay.domain.auth.repository.UserAccountSettingsRepository;
import com.epay.domain.auth.repository.UserRecordRepository;
import com.epay.domain.auth.repository.UserRepository;
import com.epay.common.exception.BadRequestException;
import com.epay.common.exception.ErrorCode;
import com.epay.common.exception.ResourceNotFoundException;
import com.epay.common.interfaces.IAuthNotificationPublisher;
import com.epay.domain.auth.dto.UserDTO;
import com.epay.domain.auth.dto.UserRecordDTO;
import com.epay.domain.auth.input.ChangePasswordRequest;
import com.epay.domain.auth.input.DeleteAccountRequest;
import com.epay.domain.auth.input.NotificationUpdateRequest;
import com.epay.domain.auth.input.PreferenceUpdateRequest;
import com.epay.domain.auth.input.UpdateProfileRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository                userRepository;
    private final UserRecordRepository          userRecordRepository;
    private final UserAccountSettingsRepository settingsRepository;
    private final KycDocumentRepository         kycDocumentRepository;
    private final NextOfKinRepository           nextOfKinRepository;
    private final PasswordEncoder               passwordEncoder;
    private final IAuthNotificationPublisher    notificationPublisher;

    public UserDTO getCurrentUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return toUserDTO(user);
    }

    public UserRecordDTO getProfile(Long userId) {
        UserRecord record = userRecordRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found"));
        return toRecordDTO(record);
    }

    public FullUserProfileDTO getFullProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        UserRecord record = userRecordRepository.findByUserId(userId).orElse(null);
        NextOfKin nok     = nextOfKinRepository.findByUserId(userId).orElse(null);
        UserAccountSettings settings = settingsRepository.findByUserId(userId).orElse(null);

        java.util.List<KycDocument> docs = kycDocumentRepository.findByUserId(userId);

        return FullUserProfileDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .username(user.getUsername())
                .role(user.getRole())
                .accountType(user.getAccountType())
                .kycTier(user.getKycTier())
                .kycStatus(user.getKycStatus())
                .enabled(user.isEnabled())
                .emailVerified(user.isEmailVerified())
                .phoneVerified(user.isPhoneVerified())
                .twoFactorEnabled(user.isTwoFactorEnabled())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .personal(record != null ? FullUserProfileDTO.PersonalInfo.builder()
                        .firstName(record.getFirstName())
                        .lastName(record.getLastName())
                        .fullName(record.getFirstName() + " " + record.getLastName())
                        .phoneNumber(record.getPhoneNumber())
                        .gender(record.getGender())
                        .dateOfBirth(record.getDateOfBirth())
                        .address(record.getAddress())
                        .city(record.getCity())
                        .state(record.getState())
                        .country(record.getCountry())
                        .countryCode(record.getCountryCode())
                        .referralCode(record.getReferralCode())
                        .referredByCode(record.getReferredByCode())
                        .totalReferrals(record.getTotalReferrals())
                        .profilePhotoUrl(record.getProfilePhotoUrl())
                        .profileComplete(record.isProfileComplete())
                        .build() : null)
                .nextOfKin(nok != null ? FullUserProfileDTO.NextOfKinInfo.builder()
                        .firstName(nok.getFirstName())
                        .lastName(nok.getLastName())
                        .relationship(nok.getRelationship())
                        .phone(nok.getPhone())
                        .email(nok.getEmail())
                        .address(nok.getAddress())
                        .build() : null)
                .kycDocuments(docs.stream().map(d ->
                        FullUserProfileDTO.KycDocumentInfo.builder()
                                .id(d.getId())
                                .documentType(d.getDocumentType())
                                .status(d.getStatus())
                                .storageReference(d.getStorageReference())
                                .documentNumber(d.getDocumentNumber())
                                .expiryDate(d.getExpiryDate())
                                .issuingCountry(d.getIssuingCountry())
                                .rejectionReason(d.getRejectionReason())
                                .uploadedAt(d.getUploadedAt())
                                .build())
                        .toList())
                .settings(settings != null ? FullUserProfileDTO.AccountSettingsInfo.builder()
                        .emailAlert(settings.getIsEmailAlert())
                        .transactionAlert(settings.getIsTransactionAlert())
                        .loginAlert(settings.getIsLoginAlert())
                        .smsMessage(settings.getIsReceiveSmsMessage())
                        .marketingNews(settings.getIsReceiveMarketingNews())
                        .biometric(settings.getIsBiometric())
                        .preferredLanguage(settings.getPreferredLanguage())
                        .timeZone(settings.getTimeZone())
                        .sessionTimeout(settings.getSessionTimeOut())
                        .build() : null)
                .build();
    }

    @Transactional
    public UserRecordDTO updateProfile(Long userId, UpdateProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        UserRecord record = userRecordRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found"));

        // ── UserRecord fields ─────────────────────────────────────────────────
        if (request.getFirstName()   != null) record.setFirstName(request.getFirstName());
        if (request.getLastName()    != null) record.setLastName(request.getLastName());
        if (request.getTelephone()   != null) record.setPhoneNumber(request.getTelephone());
        if (request.getGender()      != null) record.setGender(request.getGender().toUpperCase());
        if (request.getDob()         != null) record.setDateOfBirth(request.getDob());
        if (request.getAddress()     != null) record.setAddress(request.getAddress());
        if (request.getCity()        != null) record.setCity(request.getCity());
        if (request.getState()       != null) record.setState(request.getState());
        if (request.getCountry()     != null) record.setCountry(request.getCountry());
        if (request.getCountryCode() != null) record.setCountryCode(request.getCountryCode());

        // Mark profile complete if all key fields are now populated
        if (record.getFirstName() != null && record.getLastName() != null
                && record.getPhoneNumber() != null && record.getDateOfBirth() != null) {
            record.setProfileComplete(true);
        }

        userRecordRepository.save(record);

        // ── User entity: email update ─────────────────────────────────────────
        if (request.getEmail() != null && !request.getEmail().equalsIgnoreCase(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail()))
                throw new BadRequestException(
                        "Email address is already in use by another account",
                        ErrorCode.RESOURCE_ALREADY_EXISTS);
            // Re-use the updatePassword method slot — update email directly via save
            user.setEmail(request.getEmail());
            user.setEmailVerified(false); // require re-verification on email change
            userRepository.save(user);
            log.info("Email updated for userId={} — verification required", userId);
        }

        log.info("Profile updated: userId={}", userId);
        return toRecordDTO(record);
    }

    @Transactional
    public void updateNotificationPreferences(Long userId, NotificationUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        UserAccountSettings settings = settingsRepository.findByUserId(userId)
                .orElseGet(() -> {
                    UserAccountSettings s = new UserAccountSettings();
                    s.setUser(user);
                    return s;
                });

        if (request.getEmailNotifications() != null) settings.setIsEmailAlert(request.getEmailNotifications());
        if (request.getSmsNotifications() != null)   settings.setIsReceiveSmsMessage(request.getSmsNotifications());
        if (request.getTransactionAlerts() != null)  settings.setIsTransactionAlert(request.getTransactionAlerts());
        if (request.getLoginAlerts() != null)        settings.setIsLoginAlert(request.getLoginAlerts());
        if (request.getMarketingEmails() != null)    settings.setIsReceiveMarketingNews(request.getMarketingEmails());

        settingsRepository.save(settings);
        log.info("Notification preferences updated: userId={}", userId);
    }

    @Transactional
    public void updatePreferences(Long userId, PreferenceUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        UserAccountSettings settings = settingsRepository.findByUserId(userId)
                .orElseGet(() -> {
                    UserAccountSettings s = new UserAccountSettings();
                    s.setUser(user);
                    return s;
                });

        if (request.getLanguage() != null)              settings.setPreferredLanguage(request.getLanguage());
        if (request.getTimezone() != null)              settings.setTimeZone(request.getTimezone());
        if (request.getSessionTimeoutMinutes() != null) settings.setSessionTimeOut(
                String.valueOf(request.getSessionTimeoutMinutes()));

        settingsRepository.save(settings);
        log.info("Preferences updated: userId={}", userId);
    }

    @Transactional
    public void toggleTwoFactor(Long userId, boolean enable) {
        userRepository.updateTwoFactorEnabled(userId, enable);
        log.info("2FA {} for userId={}", enable ? "enabled" : "disabled", userId);
    }

    // ── Password update ───────────────────────────────────────────────────────

    /**
     * PUT /user/settings/update-password
     *
     * Payload: { oldPassword, password, confirmPassword }
     *
     * Validates:
     *   1. oldPassword matches the current bcrypt hash
     *   2. password == confirmPassword
     *   3. password differs from oldPassword (can't reuse same password)
     *
     * On success, hashes and stores the new password then sends a security alert.
     */
    @Transactional
    public ResponseEntity<?> updateUserPassword(ChangePasswordRequest request,
                                                 Authentication authentication) {
        // Resolve user from authentication context
        String username = authentication != null ? authentication.getName() : null;
        if (username == null)
            throw new BadRequestException("Authentication required", ErrorCode.UNAUTHORIZED_ACCESS);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // 1. Verify old password
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword()))
            throw new BadRequestException("Current password is incorrect",
                    ErrorCode.INVALID_CREDENTIALS);

        // 2. Confirm new passwords match
        if (!request.getPassword().equals(request.getConfirmPassword()))
            throw new BadRequestException("New passwords do not match", ErrorCode.INVALID_INPUT);

        // 3. New password must differ from old
        if (passwordEncoder.matches(request.getPassword(), user.getPassword()))
            throw new BadRequestException(
                    "New password must be different from your current password",
                    ErrorCode.INVALID_INPUT);

        // 4. Hash and persist
        userRepository.updatePassword(user.getId(), passwordEncoder.encode(request.getPassword()));
        log.info("Password updated: userId={}", user.getId());

        // 5. Send security alert asynchronously
        final Long userId  = user.getId();
        final String email = user.getEmail();
        userRecordRepository.findByUserId(userId).ifPresent(rec -> {
            String fullName = rec.getFirstName() + " " + rec.getLastName();
            java.util.concurrent.CompletableFuture.runAsync(() -> {
                try {
                    notificationPublisher.publishAccountSecurityAlert(
                            email, fullName, user.getUsername(),
                            "PASSWORD_CHANGED",
                            java.time.Instant.now().toString(),
                            null, null, null, null);
                } catch (Exception ex) {
                    log.warn("[UserService] Password-change alert failed: {}", ex.getMessage());
                }
            });
        });

        return ResponseEntity.ok(
                java.util.Map.of("success", true,
                        "message", "Password updated successfully."));
    }

    // ── Account lock / block (admin) ──────────────────────────────────────────

    @Transactional
    public ResponseEntity<?> lockUserAccount(Long userId) {
        userRepository.lockAccount(userId, LocalDateTime.now(),
                "Account locked by administrator");
        log.info("Account locked: userId={}", userId);
        return ResponseEntity.ok(
                java.util.Map.of("success", true, "message", "Account locked."));
    }

    @Transactional
    public ResponseEntity<?> blockUserAccount(Long userId) {
        userRepository.updateEnabled(userId, false);
        log.info("Account blocked: userId={}", userId);
        return ResponseEntity.ok(
                java.util.Map.of("success", true, "message", "Account blocked."));
    }

    @Transactional
    public void requestAccountDeletion(Long userId, DeleteAccountRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword()))
            throw new BadRequestException("Password confirmation failed", ErrorCode.INVALID_CREDENTIALS);

        userRepository.updateEnabled(userId, false);

        userRecordRepository.findByUserId(userId).ifPresent(rec -> {
            String fullName = rec.getFirstName() + " " + rec.getLastName();
            java.util.concurrent.CompletableFuture.runAsync(() -> {
                try {
                    notificationPublisher.publishAccountSecurityAlert(
                            user.getEmail(), fullName, user.getUsername(),
                            "ACCOUNT_DELETION_REQUESTED",
                            java.time.Instant.now().toString(),
                            null, null, null, null);
                } catch (Exception ex) {
                    log.warn("[UserService] Deletion alert failed: {}", ex.getMessage());
                }
            });
        });

        log.info("Account deletion requested: userId={} reason={}", userId, request.getReason());
    }

    private UserDTO toUserDTO(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .username(user.getUsername())
                .role(user.getRole())
                .accountType(user.getAccountType())
                .kycTier(user.getKycTier())
                .kycStatus(user.getKycStatus())
                .enabled(user.isEnabled())
                .emailVerified(user.isEmailVerified())
                .phoneVerified(user.isPhoneVerified())
                .twoFactorAuth(user.isTwoFactorEnabled())
                .createdOn(user.getCreatedAt())
                .updatedOn(user.getUpdatedAt())
                .build();
    }

    private UserRecordDTO toRecordDTO(UserRecord r) {
        return UserRecordDTO.builder()
                .id(r.getId())
                .firstName(r.getFirstName())
                .lastName(r.getLastName())
                .phoneNumber(r.getPhoneNumber())
                .gender(r.getGender())
                .countryCode(r.getCountryCode())
                .country(r.getCountry())
                .city(r.getCity())
                .state(r.getState())
                .dateOfBirth(r.getDateOfBirth())
                .address(r.getAddress())
                .referralCode(r.getReferralCode())
                .referredByCode(r.getReferredByCode())
                .totalReferrals(r.getTotalReferrals())
                .profilePhotoUrl(r.getProfilePhotoUrl())
                .profileComplete(r.isProfileComplete())
                .createdAt(r.getCreatedAt())
                .updatedAt(r.getUpdatedAt())
                .build();
    }
}
