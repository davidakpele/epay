package com.epay.auth.controller;

import com.epay.auth.service.UserService;
import com.epay.common.exception.ApiResponse;
import com.epay.common.exception.ResourceNotFoundException;
import com.epay.domain.auth.dto.FullUserProfileDTO;
import com.epay.domain.auth.dto.UserRecordDTO;
import com.epay.domain.auth.input.ChangePasswordRequest;
import com.epay.domain.auth.input.DeleteAccountRequest;
import com.epay.domain.auth.input.NotificationUpdateRequest;
import com.epay.domain.auth.input.PreferenceUpdateRequest;
import com.epay.domain.auth.input.UpdateProfileRequest;
import com.epay.domain.auth.repository.UserRepository;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService          userService;
    private final UserRepository       userRepository;

    @GetMapping("/profile")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<UserRecordDTO>> getProfile(
            @RequestAttribute("userId") Long userId) {
        return ResponseEntity.ok(ApiResponse.success(null,
                userService.getProfile(userId)));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<FullUserProfileDTO>> getUserById(
            @PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.success(null,
                userService.getFullProfile(userId)));
    }

    @GetMapping("/username/{username}")
    public ResponseEntity<ApiResponse<FullUserProfileDTO>> getUserByUsername(
            @PathVariable String username) {
        Long userId = userRepository.findByUsername(username)
                .map(u -> u.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return ResponseEntity.ok(ApiResponse.success(null,
                userService.getFullProfile(userId)));
    }

    /** PUT /user/profile — update own profile (authenticated user) */
    @PutMapping("/profile")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<UserRecordDTO>> updateProfile(
            @RequestAttribute("userId") Long userId,
            @Valid @RequestBody UpdateProfileRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Profile updated.",
                userService.updateProfile(userId, request)));
    }

    /**
     * PUT /user/profile/{userId} — update profile by explicit userId.
     * Accepts the frontend payload:
     *   { firstName, lastName, email, gender, address, dob, telephone, country, state, city }
     */
    @PutMapping("/profile/{userId}")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<ApiResponse<UserRecordDTO>> updateProfileById(
            @PathVariable Long userId,
            @Valid @RequestBody UpdateProfileRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Profile updated.",
                userService.updateProfile(userId, request)));
    }

    @PutMapping("/settings/notifications")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<Void>> updateNotifications(
            @RequestAttribute("userId") Long userId,
            @Valid @RequestBody NotificationUpdateRequest request) {
        userService.updateNotificationPreferences(userId, request);
        return ResponseEntity.ok(ApiResponse.success("Notification settings updated.", null));
    }

    @PutMapping("/settings/preferences")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<Void>> updatePreferences(
            @RequestAttribute("userId") Long userId,
            @Valid @RequestBody PreferenceUpdateRequest request) {
        userService.updatePreferences(userId, request);
        return ResponseEntity.ok(ApiResponse.success("Preferences updated.", null));
    }

    @PostMapping("/2fa/toggle")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<Void>> toggle2fa(
            @RequestAttribute("userId") Long userId,
            @RequestParam boolean enable) {
        userService.toggleTwoFactor(userId, enable);
        return ResponseEntity.ok(ApiResponse.success(
                "Two-factor authentication " + (enable ? "enabled" : "disabled") + ".", null));
    }

    @DeleteMapping("/account")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<Void>> deleteAccount(@RequestAttribute("userId") Long userId, @Valid @RequestBody DeleteAccountRequest request) {
        userService.requestAccountDeletion(userId, request);
        return ResponseEntity.ok(ApiResponse.success(
                "Account deletion requested. It will be processed within 30 days.", null));
    }

    @PreAuthorize("hasRole('USER')")
    @PutMapping("/settings/update-password")
    public ResponseEntity<?> updatePassword(@RequestBody ChangePasswordRequest request, Authentication authentication) {
        return ResponseEntity.ok(userService.updateUserPassword(request, authentication));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_USER')")
    @PutMapping("/{id}/account/lock")
    public ResponseEntity<?> lockUserAccount(@PathVariable("id") Long userId) {
        return userService.lockUserAccount(userId);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_USER')")
    @PutMapping("/{id}/account/block")
    public ResponseEntity<?> blockUserAccount(@PathVariable("id") Long userId) {
        return userService.blockUserAccount(userId);
    }
}
