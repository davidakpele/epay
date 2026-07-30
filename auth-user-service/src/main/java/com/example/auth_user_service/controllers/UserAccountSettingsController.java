package com.example.auth_user_service.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.auth_user_service.interfaces.IUserAccountSettingsService;
import com.example.auth_user_service.models.UserAccountSettings;
import com.example.auth_user_service.payloads.BiometricUpdateRequest;
import com.example.auth_user_service.payloads.NotificationUpdateRequest;
import com.example.auth_user_service.payloads.PreferenceUpdateRequest;
import com.example.auth_user_service.payloads.SessionTimeoutUpdateRequest;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/settings")
public class UserAccountSettingsController {

    private final IUserAccountSettingsService settingsService;

    public UserAccountSettingsController(IUserAccountSettingsService settingsService) {
        this.settingsService = settingsService;
    }

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

    @PutMapping("/{userId}/notifications")
    public ResponseEntity<Map<String, Object>> updateNotificationSettings(
            @PathVariable Long userId,
            @RequestBody NotificationUpdateRequest request) {
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

    @PutMapping("/{userId}/preferences")
    public ResponseEntity<Map<String, Object>> updatePreferences(
            @PathVariable Long userId,
            @RequestBody PreferenceUpdateRequest request) {
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
