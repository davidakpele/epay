package com.epay.auth.controller;


import com.epay.common.exception.ApiResponse;
import com.epay.common.exception.ResourceNotFoundException;
import com.epay.domain.auth.dto.UserDTO;
import com.epay.domain.auth.entity.User;
import com.epay.domain.auth.enums.KycStatus;
import com.epay.domain.auth.enums.Role;
import com.epay.domain.auth.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/admin/users")
@PreAuthorize("hasAnyRole('ADMIN','SUPER_USER')")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserRepository userRepository;

    /** GET /admin/users — paginated all users */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<UserDTO>>> getAllUsers(
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        Page<UserDTO> page = userRepository.findAll(pageable).map(this::toDTO);
        return ResponseEntity.ok(ApiResponse.success(null, page));
    }

    /** GET /admin/users/{userId} */
    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserDTO>> getUser(@PathVariable Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return ResponseEntity.ok(ApiResponse.success(null, toDTO(user)));
    }

    /** POST /admin/users/{userId}/lock */
    @PostMapping("/{userId}/lock")
    public ResponseEntity<ApiResponse<Void>> lockUser(@PathVariable Long userId,
                                                        @RequestParam String reason) {
        userRepository.lockAccount(userId, LocalDateTime.now(), reason);
        return ResponseEntity.ok(ApiResponse.success("User locked.", null));
    }

    /** POST /admin/users/{userId}/unlock */
    @PostMapping("/{userId}/unlock")
    public ResponseEntity<ApiResponse<Void>> unlockUser(@PathVariable Long userId) {
        userRepository.unlockAccount(userId);
        return ResponseEntity.ok(ApiResponse.success("User unlocked.", null));
    }

    /** POST /admin/users/{userId}/enable */
    @PostMapping("/{userId}/enable")
    public ResponseEntity<ApiResponse<Void>> enableUser(@PathVariable Long userId) {
        userRepository.updateEnabled(userId, true);
        return ResponseEntity.ok(ApiResponse.success("User enabled.", null));
    }

    /** POST /admin/users/{userId}/disable */
    @PostMapping("/{userId}/disable")
    public ResponseEntity<ApiResponse<Void>> disableUser(@PathVariable Long userId) {
        userRepository.updateEnabled(userId, false);
        return ResponseEntity.ok(ApiResponse.success("User disabled.", null));
    }

    /** GET /admin/users/kyc-pending — users pending KYC review */
    @GetMapping("/kyc-pending")
    public ResponseEntity<ApiResponse<Page<UserDTO>>> getKycPending(
            @PageableDefault(size = 20) Pageable pageable) {
        Page<UserDTO> page = userRepository.findByKycStatus(KycStatus.SUBMITTED, pageable)
                .map(this::toDTO);
        return ResponseEntity.ok(ApiResponse.success(null, page));
    }

    /** GET /admin/users/stats — dashboard counts */
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<Object>> getStats() {
        java.util.Map<String, Object> stats = new java.util.LinkedHashMap<>();
        stats.put("totalUsers",    userRepository.countByRole(Role.USER));
        stats.put("activeUsers",   userRepository.countActiveByRole(Role.USER));
        stats.put("inactiveUsers", userRepository.countInactiveByRole(Role.USER));
        stats.put("kycPending",    userRepository.countByKycStatus(KycStatus.SUBMITTED));
        stats.put("kycApproved",   userRepository.countByKycStatus(KycStatus.APPROVED));
        return ResponseEntity.ok(ApiResponse.success(null, stats));
    }

    private UserDTO toDTO(User user) {
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
}
