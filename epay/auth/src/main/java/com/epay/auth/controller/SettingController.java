package com.epay.auth.controller;

import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import com.epay.auth.service.KycService;
import com.epay.auth.service.UserService;
import com.epay.common.exception.ApiResponse;
import com.epay.common.exception.ResourceNotFoundException;
import com.epay.domain.auth.dto.FullUserProfileDTO;

@RestController
@RequestMapping("/settings")
public class SettingController {

    private final KycService  kycService;
    private final UserService userService;

    public SettingController(KycService kycService, UserService userService) {
        this.kycService  = kycService;
        this.userService = userService;
    }

    @GetMapping("/user/{id}")
    @PreAuthorize("hasRole('USER') and @security.hasValidSession()")
    public ResponseEntity<ApiResponse<FullUserProfileDTO>> findById(@PathVariable Long id) {
        if (id <= 0) {
            return ResponseEntity
                    .badRequest()
                    .body(ApiResponse.error("Invalid request: User ID must be a positive number."));
        }
        try {
            FullUserProfileDTO profile = userService.getFullProfile(id);
            return ResponseEntity.ok(ApiResponse.success(null, profile));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("User with ID " + id + " does not exist."));
        }
    }

    @PreAuthorize("hasRole('USER') and @security.hasValidSession()")
    @PostMapping(value = "/upload-profile-image/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadProfileImage(@PathVariable Long id,
                                                @RequestParam("image") MultipartFile image) {
        return kycService.uploadUserProfileImage(id, image);
    }

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
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error updating Two-Factor Authentication");
        }
    }

    @DeleteMapping("/remove-profile-image/{id}")
    public ResponseEntity<?> removeProfileImage(@PathVariable Long id) {
        return kycService.removeUserProfileImage(id);
    }
}
