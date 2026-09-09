package com.epay.admin.controller;

import com.epay.admin.service.AdminUserManagementService;
import com.epay.common.config.security.JwtClaimsHolder;
import com.epay.common.exception.ApiResponse;
import com.epay.domain.admin.input.AdminCreateUserRequest;
import com.epay.domain.admin.input.AdminUpdateUserRequest;
import com.epay.domain.auth.dto.FullUserProfileDTO;
import com.epay.domain.auth.dto.UserDTO;
import com.epay.domain.auth.enums.Role;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(name = "Admin — Users", description = "Search, create, update, and manage platform user accounts")
@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminUserManagementService userManagementService;
    private final JwtClaimsHolder            jwtClaims;

    @Operation(
        summary     = "Search users",
        description = "Full-text search across users by keyword (username or email), optionally filtered by role and enabled status."
    )
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

    @Operation(
        summary     = "Get full user profile by ID",
        description = "Returns the complete user profile including KYC tier, wallet summary, and account settings."
    )
    @GetMapping("/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_USER','CUSTOMER_SERVICE') and @security.hasPermission('user:read')")
    public ResponseEntity<ApiResponse<FullUserProfileDTO>> getUser(@PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.success(null,
                userManagementService.getUserProfile(userId)));
    }

    @Operation(
        summary     = "Create a user account (admin)",
        description = "Provisions a new user account with the specified role and profile. Used for bulk onboarding or staff account creation."
    )
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_USER') and @security.hasPermission('user:update')")
    public ResponseEntity<ApiResponse<UserDTO>> createUser(
            @Valid @RequestBody AdminCreateUserRequest request,
            Authentication auth) {
        UserDTO created = userManagementService.createUser(request, jwtClaims.getUserId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("User account created successfully.", created));
    }

    @Operation(
        summary     = "Update a user account",
        description = "Updates profile fields, email, or role for an existing user account."
    )
    @PutMapping("/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_USER') and @security.hasPermission('user:update')")
    public ResponseEntity<ApiResponse<UserDTO>> updateUser(
            @PathVariable Long userId,
            @Valid @RequestBody AdminUpdateUserRequest request) {
        return ResponseEntity.ok(ApiResponse.success("User updated.",
                userManagementService.updateUser(userId, request)));
    }

    @Operation(
        summary     = "Delete a user account",
        description = "Permanently removes the user account and all associated data. SUPER_USER only — irreversible."
    )
    @DeleteMapping("/{userId}")
    @PreAuthorize("hasRole('SUPER_USER') and @security.hasPermission('user:delete')")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long userId) {
        userManagementService.deleteUser(userId);
        return ResponseEntity.ok(ApiResponse.success("User account deleted.", null));
    }

    @Operation(
        summary     = "Block a user account",
        description = "Prevents the user from logging in. An optional reason can be recorded for compliance."
    )
    @PostMapping("/{userId}/block")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_USER','CUSTOMER_SERVICE') and @security.hasPermission('user:update')")
    public ResponseEntity<ApiResponse<Void>> blockUser(
            @PathVariable Long userId,
            @RequestParam(required = false) String reason) {
        userManagementService.blockUser(userId, reason);
        return ResponseEntity.ok(ApiResponse.success("Account blocked.", null));
    }

    @Operation(
        summary     = "Unblock a user account",
        description = "Re-enables login access for a previously blocked account."
    )
    @PostMapping("/{userId}/unblock")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_USER','CUSTOMER_SERVICE') and @security.hasPermission('user:update')")
    public ResponseEntity<ApiResponse<Void>> unblockUser(@PathVariable Long userId) {
        userManagementService.unblockUser(userId);
        return ResponseEntity.ok(ApiResponse.success("Account unblocked.", null));
    }

    @Operation(
        summary     = "Lock a user account",
        description = "Temporarily locks the account after suspicious activity. Login is blocked until manually unlocked."
    )
    @PostMapping("/{userId}/lock")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_USER') and @security.hasPermission('user:update')")
    public ResponseEntity<ApiResponse<Void>> lockUser(
            @PathVariable Long userId,
            @RequestParam(required = false) String reason) {
        userManagementService.lockUser(userId, reason);
        return ResponseEntity.ok(ApiResponse.success("Account locked.", null));
    }

    @Operation(
        summary     = "Unlock a user account",
        description = "Removes the temporary lock and restores normal login access."
    )
    @PostMapping("/{userId}/unlock")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_USER') and @security.hasPermission('user:update')")
    public ResponseEntity<ApiResponse<Void>> unlockUser(@PathVariable Long userId) {
        userManagementService.unlockUser(userId);
        return ResponseEntity.ok(ApiResponse.success("Account unlocked.", null));
    }

    @Operation(
        summary     = "Get user platform statistics",
        description = "Returns aggregated stats: total users, active/inactive counts, KYC status breakdown, and registration trend."
    )
    @GetMapping("/stats")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_USER') and @security.hasPermission('admin:access')")
    public ResponseEntity<ApiResponse<Object>> getStats() {
        return ResponseEntity.ok(ApiResponse.success(null,
                userManagementService.getDashboardStats()));
    }
}
