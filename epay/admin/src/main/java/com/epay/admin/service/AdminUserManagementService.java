package com.epay.admin.service;

import com.epay.common.exception.*;
import com.epay.common.interfaces.IAuthNotificationPublisher;
import com.epay.common.interfaces.IWalletPort;
import com.epay.domain.admin.dto.StaffDTO;
import com.epay.domain.admin.input.*;
import com.epay.domain.auth.dto.FullUserProfileDTO;
import com.epay.domain.auth.dto.UserDTO;
import com.epay.domain.auth.entity.User;
import com.epay.domain.auth.entity.UserRecord;
import com.epay.domain.auth.enums.AccountType;
import com.epay.domain.auth.enums.KycStatus;
import com.epay.domain.auth.enums.KycTier;
import com.epay.domain.auth.enums.Role;
import com.epay.domain.auth.repository.UserRecordRepository;
import com.epay.domain.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminUserManagementService {

    private final UserRepository        userRepository;
    private final UserRecordRepository  userRecordRepository;
    private final PasswordEncoder       passwordEncoder;
    private final IWalletPort           walletPort;
    private final IAuthNotificationPublisher notificationPublisher;

    @Transactional
    public StaffDTO createStaff(CreateStaffRequest request, Long createdBy) {
        if (request.getRole() == null || request.getRole() == Role.USER)
            throw new BadRequestException("Invalid role for staff account", ErrorCode.INVALID_INPUT);
        if (userRepository.existsByUsername(request.getUsername()))
            throw new ConflictException("Username already taken", ErrorCode.RESOURCE_ALREADY_EXISTS);
        if (userRepository.existsByEmail(request.getEmail()))
            throw new ConflictException("Email already registered", ErrorCode.RESOURCE_ALREADY_EXISTS);

        User user = User.builder()
                .email(request.getEmail())
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .accountType(AccountType.INDIVIDUAL)
                .kycTier(KycTier.TIER_1)
                .kycStatus(KycStatus.NOT_SUBMITTED)
                .enabled(true)
                .emailVerified(true)
                .phoneVerified(false)
                .twoFactorEnabled(false)
                .accountLocked(false)
                .build();
        userRepository.save(user);

        UserRecord record = UserRecord.builder()
                .user(user)
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phoneNumber(request.getPhone())
                .referralCode(UUID.randomUUID().toString())
                .build();
        userRecordRepository.save(record);

        log.info("[Admin] Staff created: userId={} role={} by={}", user.getId(), user.getRole(), createdBy);
        return toStaffDTO(user, record);
    }

    public StaffDTO getStaff(Long staffId) {
        User user = requireNonUser(staffId);
        UserRecord record = userRecordRepository.findByUserId(staffId).orElse(null);
        return toStaffDTO(user, record);
    }

    public Page<StaffDTO> listAllStaff(Pageable pageable) {
        return userRepository.findAllStaff(pageable)
                .map(u -> toStaffDTO(u, userRecordRepository.findByUserId(u.getId()).orElse(null)));
    }

    public Page<StaffDTO> listStaffByRole(Role role, Pageable pageable) {
        return userRepository.findStaffByRole(role, pageable)
                .map(u -> toStaffDTO(u, userRecordRepository.findByUserId(u.getId()).orElse(null)));
    }

    @Transactional
    public StaffDTO updateStaff(Long staffId, UpdateStaffRequest request, Role callerRole) {
        User user = requireNonUser(staffId);
        UserRecord record = userRecordRepository.findByUserId(staffId)
                .orElseThrow(() -> new ResourceNotFoundException("Staff profile not found"));

        if (request.getFirstName() != null) record.setFirstName(request.getFirstName());
        if (request.getLastName()  != null) record.setLastName(request.getLastName());
        if (request.getPhone()     != null) record.setPhoneNumber(request.getPhone());
        if (request.getEmail()     != null) {
            if (userRepository.existsByEmail(request.getEmail()))
                throw new ConflictException("Email already in use", ErrorCode.RESOURCE_ALREADY_EXISTS);
            user.setEmail(request.getEmail());
        }
        if (request.getEnabled() != null) user.setEnabled(request.getEnabled());

        if (request.getRole() != null) {
            if (callerRole != Role.SUPER_USER)
                throw new ForbiddenException("Only SUPER_USER can change staff roles",
                        ErrorCode.FORBIDDEN_ACCESS);
            if (request.getRole() == Role.USER)
                throw new BadRequestException("Cannot downgrade staff to USER role via this endpoint",
                        ErrorCode.INVALID_INPUT);
            user.setRole(request.getRole());
        }

        userRepository.save(user);
        userRecordRepository.save(record);
        log.info("[Admin] Staff updated: staffId={}", staffId);
        return toStaffDTO(user, record);
    }

    @Transactional
    public void changeStaffRole(Long staffId, Role newRole, Long callerUserId) {
        if (newRole == Role.USER)
            throw new BadRequestException("Cannot assign USER role to staff via this endpoint",
                    ErrorCode.INVALID_INPUT);
        User user = requireNonUser(staffId);
        if (user.getId().equals(callerUserId))
            throw new BadRequestException("Cannot change your own role", ErrorCode.INVALID_INPUT);
        userRepository.updateRole(staffId, newRole);
        log.info("[Admin] Role changed: staffId={} newRole={} by={}", staffId, newRole, callerUserId);
    }

    @Transactional
    public void deleteStaff(Long staffId, Long callerUserId) {
        User user = requireNonUser(staffId);
        if (user.getId().equals(callerUserId))
            throw new BadRequestException("Cannot delete your own account", ErrorCode.INVALID_INPUT);
        userRepository.updateEnabled(staffId, false);
        userRepository.lockAccount(staffId, LocalDateTime.now(), "Account removed by super admin");
        log.info("[Admin] Staff deleted (disabled): staffId={} by={}", staffId, callerUserId);
    }

    public Page<UserDTO> searchUsers(String keyword, Role role, Boolean enabled, Pageable pageable) {
        return userRepository.adminSearch(keyword, role, enabled, pageable).map(this::toUserDTO);
    }

    public FullUserProfileDTO getUserProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        UserRecord record = userRecordRepository.findByUserId(userId).orElse(null);
        return buildFullProfile(user, record);
    }

    @Transactional
    public UserDTO createUser(AdminCreateUserRequest request, Long createdBy) {
        if (userRepository.existsByUsername(request.getUsername()))
            throw new ConflictException("Username already taken", ErrorCode.RESOURCE_ALREADY_EXISTS);
        if (userRepository.existsByEmail(request.getEmail()))
            throw new ConflictException("Email already registered", ErrorCode.RESOURCE_ALREADY_EXISTS);

        User user = User.builder()
                .email(request.getEmail())
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER)
                .accountType(request.getAccountType() != null ? request.getAccountType() : AccountType.INDIVIDUAL)
                .kycTier(KycTier.TIER_1)
                .kycStatus(KycStatus.NOT_SUBMITTED)
                .enabled(true)
                .emailVerified(true)
                .phoneVerified(request.getPhone() != null)
                .twoFactorEnabled(false)
                .accountLocked(false)
                .build();
        userRepository.save(user);

        UserRecord record = UserRecord.builder()
                .user(user)
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phoneNumber(request.getPhone())
                .referralCode(UUID.randomUUID().toString())
                .build();
        userRecordRepository.save(record);

        final Long newUserId       = user.getId();
        final String defaultCurrency = request.getDefaultCurrency() != null
                ? request.getDefaultCurrency().toUpperCase() : "NGN";

        if (request.isCreateWallet()) {
            CompletableFuture.runAsync(() -> {
                try {
                    walletPort.createWalletForUser(newUserId, defaultCurrency);
                    log.info("[Admin] Wallet created for userId={}", newUserId);
                } catch (Exception ex) {
                    log.warn("[Admin] Wallet creation failed for userId={}: {}", newUserId, ex.getMessage());
                }
            });
        }

        log.info("[Admin] User created: userId={} by={}", newUserId, createdBy);
        return toUserDTO(user);
    }

    @Transactional
    public UserDTO updateUser(Long userId, AdminUpdateUserRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        UserRecord record = userRecordRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User profile not found"));

        if (request.getFirstName()    != null) record.setFirstName(request.getFirstName());
        if (request.getLastName()     != null) record.setLastName(request.getLastName());
        if (request.getPhone()        != null) record.setPhoneNumber(request.getPhone());
        if (request.getGender()       != null) record.setGender(request.getGender().toUpperCase());
        if (request.getAddress()      != null) record.setAddress(request.getAddress());
        if (request.getCity()         != null) record.setCity(request.getCity());
        if (request.getState()        != null) record.setState(request.getState());
        if (request.getCountry()      != null) record.setCountry(request.getCountry());
        if (request.getCountryCode()  != null) record.setCountryCode(request.getCountryCode());
        if (request.getDateOfBirth()  != null) {
            try {
                record.setDateOfBirth(java.time.LocalDate.parse(request.getDateOfBirth()));
            } catch (Exception ignored) {
                throw new BadRequestException("Invalid date format — use yyyy-MM-dd", ErrorCode.INVALID_INPUT);
            }
        }

        if (request.getEmail() != null && !request.getEmail().equalsIgnoreCase(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail()))
                throw new ConflictException("Email already in use", ErrorCode.RESOURCE_ALREADY_EXISTS);
            user.setEmail(request.getEmail());
        }
        if (request.getAccountType() != null) user.setAccountType(request.getAccountType());
        if (request.getKycTier()     != null) user.setKycTier(request.getKycTier());
        if (request.getKycStatus()   != null) user.setKycStatus(request.getKycStatus());
        if (request.getEmailVerified() != null) user.setEmailVerified(request.getEmailVerified());
        if (request.getPhoneVerified() != null) user.setPhoneVerified(request.getPhoneVerified());

        userRepository.save(user);
        userRecordRepository.save(record);
        log.info("[Admin] User updated: userId={}", userId);
        return toUserDTO(user);
    }

    @Transactional
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (user.getRole() != Role.USER)
            throw new BadRequestException(
                    "Cannot delete staff accounts via this endpoint — use the staff delete endpoint",
                    ErrorCode.INVALID_INPUT);
        userRecordRepository.findByUserId(userId).ifPresent(userRecordRepository::delete);
        userRepository.delete(user);
        log.info("[Admin] User hard-deleted: userId={}", userId);
    }

    @Transactional
    public void blockUser(Long userId, String reason) {
        requireExists(userId);
        userRepository.updateEnabled(userId, false);
        userRepository.lockAccount(userId, LocalDateTime.now(),
                reason != null ? reason : "Blocked by administrator");
        log.info("[Admin] User blocked: userId={}", userId);
    }

    @Transactional
    public void unblockUser(Long userId) {
        requireExists(userId);
        userRepository.updateEnabled(userId, true);
        userRepository.unlockAccount(userId);
        log.info("[Admin] User unblocked: userId={}", userId);
    }

    @Transactional
    public void lockUser(Long userId, String reason) {
        requireExists(userId);
        userRepository.lockAccount(userId, LocalDateTime.now(),
                reason != null ? reason : "Locked by administrator");
        log.info("[Admin] User locked: userId={}", userId);
    }

    @Transactional
    public void unlockUser(Long userId) {
        requireExists(userId);
        userRepository.unlockAccount(userId);
        log.info("[Admin] User unlocked: userId={}", userId);
    }

    public java.util.Map<String, Object> getDashboardStats() {
        java.util.Map<String, Object> stats = new java.util.LinkedHashMap<>();
        stats.put("totalUsers",          userRepository.countByRole(Role.USER));
        stats.put("activeUsers",         userRepository.countActiveByRole(Role.USER));
        stats.put("inactiveUsers",       userRepository.countInactiveByRole(Role.USER));
        stats.put("totalAdmins",         userRepository.countByRole(Role.ADMIN));
        stats.put("totalSuperUsers",     userRepository.countByRole(Role.SUPER_USER));
        stats.put("totalCustomerService",userRepository.countByRole(Role.CUSTOMER_SERVICE));
        stats.put("totalEditors",        userRepository.countByRole(Role.EDITOR));
        stats.put("kycPending",          userRepository.countByKycStatus(com.epay.domain.auth.enums.KycStatus.SUBMITTED));
        stats.put("kycApproved",         userRepository.countByKycStatus(com.epay.domain.auth.enums.KycStatus.APPROVED));
        return stats;
    }

    private User requireNonUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Staff member not found"));
        if (user.getRole() == Role.USER)
            throw new BadRequestException("Target account is not a staff member", ErrorCode.INVALID_INPUT);
        return user;
    }

    private void requireExists(Long userId) {
        if (!userRepository.existsById(userId))
            throw new ResourceNotFoundException("User not found");
    }

    private StaffDTO toStaffDTO(User user, UserRecord record) {
        return StaffDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(record != null ? record.getFirstName() : null)
                .lastName(record  != null ? record.getLastName()  : null)
                .phone(record     != null ? record.getPhoneNumber(): null)
                .role(user.getRole())
                .enabled(user.isEnabled())
                .accountLocked(user.isAccountLocked())
                .accountLockedReason(user.getAccountLockedReason())
                .lastLoginAt(user.getLastLoginAt())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
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

    private FullUserProfileDTO buildFullProfile(User user, UserRecord record) {
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
                        .profilePhotoUrl(record.getProfilePhotoUrl())
                        .profileComplete(record.isProfileComplete())
                        .build() : null)
                .build();
    }
}
