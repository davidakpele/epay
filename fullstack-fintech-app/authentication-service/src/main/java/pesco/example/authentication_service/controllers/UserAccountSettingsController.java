package pesco.example.authentication_service.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pesco.example.authentication_service.models.UserAccountSettings;
import pesco.example.authentication_service.payloads.BiometricUpdateRequest;
import pesco.example.authentication_service.payloads.SessionTimeoutUpdateRequest;
import pesco.example.authentication_service.servicesImplementation.UserAccountSettingsService;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/settings")
public class UserAccountSettingsController {

    private final UserAccountSettingsService settingsService;

    public UserAccountSettingsController(UserAccountSettingsService settingsService) {
        this.settingsService = settingsService;
    }

    /**
     * Get user settings
     * GET /api/user/settings/{userId}
     */
    @GetMapping("/{userId}")
    public ResponseEntity<Map<String, Object>> getUserSettings(@PathVariable Long userId) {
        try {
            UserAccountSettings settings = settingsService.findByUserId(userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("data", settings);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

    /**
     * Update biometric authentication status
     * PUT /api/user/settings/{userId}/biometric
     */
    @PutMapping("/{userId}/biometric")
    public ResponseEntity<Map<String, Object>> updateBiometricStatus(
            @PathVariable Long userId,
            @RequestBody BiometricUpdateRequest request) {
        try {
            UserAccountSettings settings = settingsService.updateBiometricStatus(
                    userId, 
                    request.getEnableBiometric()
            );
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Biometric authentication " + 
                    (request.getEnableBiometric() ? "enabled" : "disabled"));
            response.put("data", settings);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    /**
     * Update session timeout
     * PUT /api/user/settings/{userId}/session-timeout
     */
    @PutMapping("/{userId}/session-timeout")
    public ResponseEntity<Map<String, Object>> updateSessionTimeout(
            @PathVariable Long userId,
            @RequestBody SessionTimeoutUpdateRequest request) {
        try {
            UserAccountSettings settings = settingsService.updateSessionTimeout(
                    userId, 
                    request.getSessionTimeout()
            );
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Session timeout updated");
            response.put("data", settings);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    /**
     * Update notification settings
     * PUT /api/user/settings/{userId}/notifications
     */
    @PutMapping("/{userId}/notifications")
    public ResponseEntity<Map<String, Object>> updateNotificationSettings(
            @PathVariable Long userId,
            @RequestBody UserAccountSettingsService.NotificationUpdateRequest request) {
        try {
            UserAccountSettings settings = settingsService.updateNotificationSettings(userId, request);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Notification preferences updated");
            response.put("data", settings);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    /**
     * Update user preferences (language, timezone)
     * PUT /api/user/settings/{userId}/preferences
     */
    @PutMapping("/{userId}/preferences")
    public ResponseEntity<Map<String, Object>> updatePreferences(
            @PathVariable Long userId,
            @RequestBody UserAccountSettingsService.PreferenceUpdateRequest request) {
        try {
            UserAccountSettings settings = settingsService.updatePreferences(userId, request);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Preferences updated successfully");
            response.put("data", settings);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }
}