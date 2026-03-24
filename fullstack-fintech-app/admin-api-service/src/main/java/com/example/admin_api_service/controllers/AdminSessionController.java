package com.example.admin_api_service.controllers;

import com.example.admin_api_service.Interfaces.IAdminSessionService;
import com.example.admin_api_service.dto.ApiResponse;
import com.example.admin_api_service.enums.SessionTerminationReason;
import com.example.admin_api_service.models.accessAndSecurity.AdminSession;
import com.example.admin_api_service.payloads.RevokeSessionRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/admin/sessions")
public class AdminSessionController {

    private final IAdminSessionService adminSessionService;

    public AdminSessionController(IAdminSessionService adminSessionService) {
        this.adminSessionService = adminSessionService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<AdminSession>>> getAllSessions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Page<AdminSession> sessions = adminSessionService.getSessionsByAdminUser(
                null, PageRequest.of(page, size, Sort.by("createdOn").descending()));
        return ResponseEntity.ok(ApiResponse.success(sessions));
    }

    @GetMapping("/{sessionId}")
    public ResponseEntity<ApiResponse<AdminSession>> getSessionById(
            @PathVariable String sessionId) {
        return ResponseEntity.ok(ApiResponse.success(
                adminSessionService.getSessionById(sessionId)));
    }

    @GetMapping("/admin/{adminUserId}")
    public ResponseEntity<ApiResponse<Page<AdminSession>>> getSessionsByAdmin(
            @PathVariable Long adminUserId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Page<AdminSession> sessions = adminSessionService.getSessionsByAdminUser(
                adminUserId, PageRequest.of(page, size, Sort.by("createdOn").descending()));
        return ResponseEntity.ok(ApiResponse.success(sessions));
    }

    @GetMapping("/admin/{adminUserId}/active")
    public ResponseEntity<ApiResponse<List<AdminSession>>> getActiveSessionsByAdmin(
            @PathVariable Long adminUserId) {
        return ResponseEntity.ok(ApiResponse.success(
                adminSessionService.getActiveSessionsByAdminUser(adminUserId)));
    }

    @DeleteMapping("/{sessionId}")
    public ResponseEntity<ApiResponse<Void>> revokeSession(
            @PathVariable String sessionId,
            @Valid @RequestBody RevokeSessionRequest request,
            @AuthenticationPrincipal String adminId) {

        adminSessionService.revokeSession(sessionId, adminId, request.getReason());
        return ResponseEntity.ok(ApiResponse.success(null, "Session revoked successfully"));
    }

    @DeleteMapping("/admin/{adminUserId}/all")
    public ResponseEntity<ApiResponse<Void>> revokeAllSessionsForAdmin(
            @PathVariable Long adminUserId,
            @AuthenticationPrincipal String adminId) {

        adminSessionService.revokeAllSessionsForAdminUser(
                adminUserId, SessionTerminationReason.ADMIN_REVOKED);
        return ResponseEntity.ok(ApiResponse.success(null, "All sessions revoked"));
    }

    @DeleteMapping("/admin/{adminUserId}/others")
    public ResponseEntity<ApiResponse<Void>> revokeOtherSessions(
            @PathVariable Long adminUserId,
            @RequestParam String currentSessionId,
            @AuthenticationPrincipal String adminId) {

        adminSessionService.revokeAllSessionsExcept(
                adminUserId, currentSessionId, SessionTerminationReason.FORCED_LOGOUT);
        return ResponseEntity.ok(ApiResponse.success(null, "Other sessions revoked"));
    }

    @GetMapping("/{accessToken}/validate")
    public ResponseEntity<ApiResponse<Boolean>> validateSession(
            @PathVariable String accessToken) {
        return ResponseEntity.ok(ApiResponse.success(
                adminSessionService.isSessionValid(accessToken)));
    }
}