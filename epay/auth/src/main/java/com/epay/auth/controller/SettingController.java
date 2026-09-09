package com.epay.auth.controller;

import java.util.Map;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.epay.auth.service.KycService;
import com.epay.auth.service.UserService;
import com.epay.common.exception.ApiResponse;
import com.epay.common.exception.ResourceNotFoundException;
import com.epay.domain.auth.dto.FullUserProfileDTO;

@Tag(name = "Settings", description = "Profile image upload, 2FA setup, and account settings management")
@Slf4j
@RestController
@RequestMapping("/settings")
public class SettingController {

    private final KycService  kycService;
    private final UserService userService;

    public SettingController(KycService kycService, UserService userService) {
        this.kycService  = kycService;
        this.userService = userService;
    }

    @Operation(
        summary     = "Get full user profile by ID",
        description = "Returns the complete profile for the given user ID. Requires a valid session."
    )
    @GetMapping("/user/{id}")
    @PreAuthorize("hasRole('USER') and @security.hasValidSession()")
    public ResponseEntity<ApiResponse<FullUserProfileDTO>> findById(@PathVariable Long id) {
        if (id <= 0) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Invalid request: User ID must be a positive number."));
        }
        try {
            FullUserProfileDTO profile = userService.getFullProfile(id);
            return ResponseEntity.ok(ApiResponse.success(null, profile));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("User with ID " + id + " does not exist."));
        }
    }

    @Operation(
        summary     = "Upload profile image",
        description = "Accepts a multipart image file and sets it as the user's profile photo."
    )
    @PreAuthorize("hasRole('USER') and @security.hasValidSession()")
    @PostMapping(value = "/upload-profile-image/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadProfileImage(@PathVariable Long id,
                                                @RequestParam("image") MultipartFile image) {
        return kycService.uploadUserProfileImage(id, image);
    }

    @Operation(
        summary     = "Enable or disable two-factor authentication",
        description = "Toggles TOTP-based 2FA on or off. Expects `{\"enable2FA\": true|false}` in the body."
    )
    @PreAuthorize("hasRole('USER') and @security.hasValidSession()")
    @PostMapping("/enable-twofactor")
    public ResponseEntity<?> verifyUserOtp(@RequestBody Map<String, Boolean> requestPayload,
                                            Authentication authentication) {
        Boolean enable2FA = requestPayload.get("enable2FA");
        if (enable2FA == null) {
            return ResponseEntity.badRequest().body("Invalid request payload");
        }
        try {
            return ResponseEntity.ok(kycService.enableUserTwoFactorKey(enable2FA, authentication));
        } catch (Exception e) {
            log.error("[Settings] Failed to update 2FA for user {}: {}",
                    authentication.getName(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error updating Two-Factor Authentication");
        }
    }

    @Operation(
        summary     = "Remove profile image",
        description = "Deletes the current profile photo and resets it to the default avatar."
    )
    @DeleteMapping("/remove-profile-image/{id}")
    public ResponseEntity<?> removeProfileImage(@PathVariable Long id) {
        return kycService.removeUserProfileImage(id);
    }
}
