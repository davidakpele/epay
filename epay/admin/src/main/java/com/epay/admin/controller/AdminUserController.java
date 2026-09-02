package com.epay.admin.controller;

import com.epay.admin.service.AdminUserManagementService;
import com.epay.common.config.security.JwtClaimsHolder;
import com.epay.common.exception.ApiResponse;
import com.epay.domain.admin.input.AdminCreateUserRequest;
import com.epay.domain.admin.input.AdminUpdateUserRequest;
import com.epay.domain.auth.dto.FullUserProfileDTO;
import com.epay.domain.auth.dto.UserDTO;
import com.epay.domain.auth.enums.Role;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminUserManagementService userManagementService;
    private final JwtClaimsHolder            jwtClaims;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_USER','CUSTOMER_SERVICE') and @security.hasPermission('user:read')")
    public ResponseEntity<ApiResponse<Page<UserDTO>>> searchUsers(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Role role,
            @RequestParam(required = false) Boolean enabled,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(null,
                userManagementService.searchUsers(keyword, role, enabled, pageable)));
    }

    @GetMapping("/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_USER','CUSTOMER_SERVICE') and @security.hasPermission('user:read')")
    public ResponseEntity<ApiResponse<FullUserProfileDTO>> getUser(@PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.success(null,
                userManagementService.getUserProfile(userId)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_USER') and @security.hasPermission('user:update')")
    public ResponseEntity<ApiResponse<UserDTO>> createUser(
            @Valid @RequestBody AdminCreateUserRequest request,
            Authentication auth) {
        UserDTO created = userManagementService.createUser(request, jwtClaims.getUserId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("User account created successfully.", created));
    }

    @PutMapping("/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_USER') and @security.hasPermission('user:update')")
    public ResponseEntity<ApiResponse<UserDTO>> updateUser(
            @PathVariable Long userId,
            @Valid @RequestBody AdminUpdateUserRequest request) {
        return ResponseEntity.ok(ApiResponse.success("User updated.",
                userManagementService.updateUser(userId, request)));
    }

    @DeleteMapping("/{userId}")
    @PreAuthorize("hasRole('SUPER_USER') and @security.hasPermission('user:delete')")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long userId) {
        userManagementService.deleteUser(userId);
        return ResponseEntity.ok(ApiResponse.success("User account deleted.", null));
    }

    @PostMapping("/{userId}/block")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_USER','CUSTOMER_SERVICE') and @security.hasPermission('user:update')")
    public ResponseEntity<ApiResponse<Void>> blockUser(
            @PathVariable Long userId,
            @RequestParam(required = false) String reason) {
        userManagementService.blockUser(userId, reason);
        return ResponseEntity.ok(ApiResponse.success("Account blocked.", null));
    }

    @PostMapping("/{userId}/unblock")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_USER','CUSTOMER_SERVICE') and @security.hasPermission('user:update')")
    public ResponseEntity<ApiResponse<Void>> unblockUser(@PathVariable Long userId) {
        userManagementService.unblockUser(userId);
        return ResponseEntity.ok(ApiResponse.success("Account unblocked.", null));
    }

    @PostMapping("/{userId}/lock")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_USER') and @security.hasPermission('user:update')")
    public ResponseEntity<ApiResponse<Void>> lockUser(
            @PathVariable Long userId,
            @RequestParam(required = false) String reason) {
        userManagementService.lockUser(userId, reason);
        return ResponseEntity.ok(ApiResponse.success("Account locked.", null));
    }

    @PostMapping("/{userId}/unlock")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_USER') and @security.hasPermission('user:update')")
    public ResponseEntity<ApiResponse<Void>> unlockUser(@PathVariable Long userId) {
        userManagementService.unlockUser(userId);
        return ResponseEntity.ok(ApiResponse.success("Account unlocked.", null));
    }

    @GetMapping("/stats")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_USER') and @security.hasPermission('admin:access')")
    public ResponseEntity<ApiResponse<Object>> getStats() {
        return ResponseEntity.ok(ApiResponse.success(null,
                userManagementService.getDashboardStats()));
    }
}
