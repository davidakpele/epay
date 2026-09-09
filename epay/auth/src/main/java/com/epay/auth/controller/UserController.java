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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Tag(name = "User Profile", description = "Manage user profile, settings, and account lifecycle")
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService    userService;
    private final UserRepository userRepository;

    @Operation(summary = "Get own profile", description = "Returns the authenticated user's profile record.")
    @GetMapping("/profile")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<UserRecordDTO>> getProfile(
            @RequestAttribute("userId") Long userId) {
        return ResponseEntity.ok(ApiResponse.success(null, userService.getProfile(userId)));
    }

    @Operation(summary = "Get user by ID", description = "Returns a full user profile for any user ID — admin or owner use.")
    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<FullUserProfileDTO>> getUserById(@PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.success(null, userService.getFullProfile(userId)));
    }

    @Operation(summary = "Get user by username", description = "Looks up a full user profile by username.")
    @GetMapping("/username/{username}")
    public ResponseEntity<ApiResponse<FullUserProfileDTO>> getUserByUsername(
            @PathVariable String username) {
        Long userId = userRepository.findByUsername(username)
                .map(u -> u.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return ResponseEntity.ok(ApiResponse.success(null, userService.getFullProfile(userId)));
    }

    @Operation(summary = "Update own profile", description = "Updates the authenticated user's personal information.")
    @PutMapping("/profile")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<UserRecordDTO>> updateProfile(
            @RequestAttribute("userId") Long userId,
            @Valid @RequestBody UpdateProfileRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Profile updated.",
                userService.updateProfile(userId, request)));
    }

    @Operation(summary = "Update profile by ID", description = "Updates a user's profile — accessible by the user or an admin.")
    @PutMapping("/profile/{userId}")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<ApiResponse<UserRecordDTO>> updateProfileById(
            @PathVariable Long userId,
            @Valid @RequestBody UpdateProfileRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Profile updated.",
                userService.updateProfile(userId, request)));
    }

    @Operation(summary = "Update notification preferences", description = "Enables or disables email, SMS, login-alert, and marketing notifications.")
    @PutMapping("/settings/notifications")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<Void>> updateNotifications(
            @RequestAttribute("userId") Long userId,
            @Valid @RequestBody NotificationUpdateRequest request) {
        userService.updateNotificationPreferences(userId, request);
        return ResponseEntity.ok(ApiResponse.success("Notification settings updated.", null));
    }

    @Operation(summary = "Update app preferences", description = "Updates language, timezone, and other UX preferences.")
    @PutMapping("/settings/preferences")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<Void>> updatePreferences(
            @RequestAttribute("userId") Long userId,
            @Valid @RequestBody PreferenceUpdateRequest request) {
        userService.updatePreferences(userId, request);
        return ResponseEntity.ok(ApiResponse.success("Preferences updated.", null));
    }

    @Operation(summary = "Toggle two-factor authentication", description = "Enables or disables TOTP-based 2FA for the authenticated user.")
    @PostMapping("/2fa/toggle")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<Void>> toggle2fa(
            @RequestAttribute("userId") Long userId,
            @RequestParam boolean enable) {
        userService.toggleTwoFactor(userId, enable);
        return ResponseEntity.ok(ApiResponse.success(
                "Two-factor authentication " + (enable ? "enabled" : "disabled") + ".", null));
    }

    @Operation(summary = "Request account deletion", description = "Schedules the authenticated user's account for deletion within 30 days.")
    @DeleteMapping("/account")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<Void>> deleteAccount(
            @RequestAttribute("userId") Long userId,
            @Valid @RequestBody DeleteAccountRequest request) {
        userService.requestAccountDeletion(userId, request);
        return ResponseEntity.ok(ApiResponse.success(
                "Account deletion requested. It will be processed within 30 days.", null));
    }

    @Operation(summary = "Change password", description = "Updates the authenticated user's password after verifying the current one.")
    @PreAuthorize("hasRole('USER')")
    @PutMapping("/settings/update-password")
    public ResponseEntity<?> updatePassword(
            @RequestBody ChangePasswordRequest request, Authentication authentication) {
        return ResponseEntity.ok(userService.updateUserPassword(request, authentication));
    }

    @Operation(summary = "Lock a user account (admin)", description = "Temporarily locks a user account preventing login.")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_USER')")
    @PutMapping("/{id}/account/lock")
    public ResponseEntity<?> lockUserAccount(@PathVariable("id") Long userId) {
        return userService.lockUserAccount(userId);
    }

    @Operation(summary = "Block a user account (admin)", description = "Permanently blocks a user account until an admin unblocks it.")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_USER')")
    @PutMapping("/{id}/account/block")
    public ResponseEntity<?> blockUserAccount(@PathVariable("id") Long userId) {
        return userService.blockUserAccount(userId);
    }
}
