package com.epay.auth.controller;

import com.epay.common.exception.ApiResponse;
import com.epay.common.exception.ResourceNotFoundException;
import com.epay.domain.auth.dto.UserDTO;
import com.epay.domain.auth.dto.UserRecordDTO;
import com.epay.domain.auth.entity.User;
import com.epay.domain.auth.entity.UserRecord;
import com.epay.domain.auth.input.UpdateProfileRequest;
import com.epay.domain.auth.repository.UserRecordRepository;
import com.epay.domain.auth.repository.UserRepository;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository       userRepository;
    private final UserRecordRepository userRecordRepository;

    /** GET /user/profile — get the authenticated user's profile */
    @GetMapping("/profile")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<UserRecordDTO>> getProfile(
            @RequestAttribute("userId") Long userId) {

        UserRecord record = userRecordRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found"));

        UserRecordDTO dto = toRecordDTO(record);
        return ResponseEntity.ok(ApiResponse.success(null, dto));
    }

    /** GET /user/{userId} — get a user by ID (public lookup) */
    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserDTO>> getUserById(@PathVariable Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return ResponseEntity.ok(ApiResponse.success(null, toUserDTO(user)));
    }

    /** GET /user/username/{username} — find by username (public lookup) */
    @GetMapping("/username/{username}")
    public ResponseEntity<ApiResponse<UserDTO>> getUserByUsername(@PathVariable String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return ResponseEntity.ok(ApiResponse.success(null, toUserDTO(user)));
    }

    /** PUT /user/profile — update the authenticated user's profile */
    @PutMapping("/profile")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<UserRecordDTO>> updateProfile(
            @RequestAttribute("userId") Long userId,
            @Valid @RequestBody UpdateProfileRequest request) {

        UserRecord record = userRecordRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found"));

        if (request.getFirstName() != null)  record.setFirstName(request.getFirstName());
        if (request.getLastName() != null)   record.setLastName(request.getLastName());
        if (request.getPhoneNumber() != null) record.setPhoneNumber(request.getPhoneNumber());
        if (request.getGender() != null)     record.setGender(request.getGender());
        if (request.getDateOfBirth() != null) record.setDateOfBirth(request.getDateOfBirth());
        if (request.getAddress() != null)    record.setAddress(request.getAddress());
        if (request.getCity() != null)       record.setCity(request.getCity());
        if (request.getState() != null)      record.setState(request.getState());
        if (request.getCountry() != null)    record.setCountry(request.getCountry());
        if (request.getCountryCode() != null) record.setCountryCode(request.getCountryCode());

        userRecordRepository.save(record);
        return ResponseEntity.ok(ApiResponse.success("Profile updated successfully.", toRecordDTO(record)));
    }

    // ── Mappers ──────────────────────────────────────────────────────────────

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
