package com.epay.admin.controller;

import com.epay.admin.service.AdminUserManagementService;
import com.epay.common.config.security.JwtClaimsHolder;
import com.epay.common.exception.ApiResponse;
import com.epay.domain.admin.dto.StaffDTO;
import com.epay.domain.admin.input.CreateStaffRequest;
import com.epay.domain.admin.input.UpdateStaffRequest;
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

@Tag(name = "Super Admin — Staff", description = "Manage staff accounts, roles, and platform-wide statistics")
@RestController
@RequestMapping("/admin/super")
@PreAuthorize("hasRole('SUPER_USER')")
@RequiredArgsConstructor
public class SuperAdminController {

    private final AdminUserManagementService userManagementService;
    private final JwtClaimsHolder            jwtClaims;

    @Operation(
        summary     = "Create a staff account",
        description = "Provisions a new admin, customer-service, or editor account. SUPER_USER only."
    )
    @PostMapping("/staff")
    public ResponseEntity<ApiResponse<StaffDTO>> createStaff(
            @Valid @RequestBody CreateStaffRequest request,
            Authentication auth) {
        StaffDTO staff = userManagementService.createStaff(request, extractUserId(auth));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Staff account created successfully.", staff));
    }

    @Operation(
        summary     = "List all staff accounts",
        description = "Returns every staff account (ADMIN, CUSTOMER_SERVICE, EDITOR, SUPER_USER) paginated by creation date."
    )
    @GetMapping("/staff")
    public ResponseEntity<ApiResponse<Page<StaffDTO>>> listAllStaff(
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(null,
                userManagementService.listAllStaff(pageable)));
    }

    @Operation(
        summary     = "List staff by role",
        description = "Returns all staff accounts with the given role (ADMIN, CUSTOMER_SERVICE, EDITOR, SUPER_USER)."
    )
    @GetMapping("/staff/role/{role}")
    public ResponseEntity<ApiResponse<Page<StaffDTO>>> listStaffByRole(
            @PathVariable Role role,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(null,
                userManagementService.listStaffByRole(role, pageable)));
    }

    @Operation(
        summary     = "Get a staff account by ID",
        description = "Returns the full profile of a single staff member."
    )
    @GetMapping("/staff/{staffId}")
    public ResponseEntity<ApiResponse<StaffDTO>> getStaff(@PathVariable Long staffId) {
        return ResponseEntity.ok(ApiResponse.success(null,
                userManagementService.getStaff(staffId)));
    }

    @Operation(
        summary     = "Update a staff account",
        description = "Updates name, email, or contact details for an existing staff member."
    )
    @PutMapping("/staff/{staffId}")
    public ResponseEntity<ApiResponse<StaffDTO>> updateStaff(
            @PathVariable Long staffId,
            @Valid @RequestBody UpdateStaffRequest request,
            Authentication auth) {
        StaffDTO updated = userManagementService.updateStaff(staffId, request, Role.SUPER_USER);
        return ResponseEntity.ok(ApiResponse.success("Staff account updated.", updated));
    }

    @Operation(
        summary     = "Change a staff member's role",
        description = "Promotes or demotes a staff member to a different role (e.g. from ADMIN to CUSTOMER_SERVICE)."
    )
    @PatchMapping("/staff/{staffId}/role")
    public ResponseEntity<ApiResponse<Void>> changeRole(
            @PathVariable Long staffId,
            @RequestParam Role role,
            Authentication auth) {
        userManagementService.changeStaffRole(staffId, role, extractUserId(auth));
        return ResponseEntity.ok(ApiResponse.success("Role updated to " + role.name() + ".", null));
    }

    @Operation(
        summary     = "Delete a staff account",
        description = "Deactivates a staff account, preventing further login. The account record is retained for audit."
    )
    @DeleteMapping("/staff/{staffId}")
    public ResponseEntity<ApiResponse<Void>> deleteStaff(
            @PathVariable Long staffId, Authentication auth) {
        userManagementService.deleteStaff(staffId, extractUserId(auth));
        return ResponseEntity.ok(ApiResponse.success("Staff account deactivated.", null));
    }

    @Operation(
        summary     = "Get platform-wide dashboard statistics",
        description = "Returns aggregated platform stats: total users, active staff, transaction volume, and KYC breakdown."
    )
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<Object>> getStats() {
        return ResponseEntity.ok(ApiResponse.success(null,
                userManagementService.getDashboardStats()));
    }

    private Long extractUserId(Authentication auth) {
        Long id = jwtClaims.getUserId();
        return id != null ? id : 0L;
    }
}
