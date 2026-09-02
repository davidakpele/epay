package com.epay.admin.controller;

import com.epay.admin.service.AdminUserManagementService;
import com.epay.common.config.security.JwtClaimsHolder;
import com.epay.common.exception.ApiResponse;
import com.epay.domain.admin.dto.StaffDTO;
import com.epay.domain.admin.input.CreateStaffRequest;
import com.epay.domain.admin.input.UpdateStaffRequest;
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
@RequestMapping("/admin/super")
@PreAuthorize("hasRole('SUPER_USER')")
@RequiredArgsConstructor
public class SuperAdminController {

    private final AdminUserManagementService userManagementService;
    private final JwtClaimsHolder            jwtClaims;


    @PostMapping("/staff")
    public ResponseEntity<ApiResponse<StaffDTO>> createStaff(
            @Valid @RequestBody CreateStaffRequest request,
            Authentication auth) {
        Long callerId = extractUserId(auth);
        StaffDTO staff = userManagementService.createStaff(request, callerId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Staff account created successfully.", staff));
    }

    @GetMapping("/staff")
    public ResponseEntity<ApiResponse<Page<StaffDTO>>> listAllStaff(
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(null,
                userManagementService.listAllStaff(pageable)));
    }

    @GetMapping("/staff/role/{role}")
    public ResponseEntity<ApiResponse<Page<StaffDTO>>> listStaffByRole(
            @PathVariable Role role,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(null,
                userManagementService.listStaffByRole(role, pageable)));
    }


    @GetMapping("/staff/{staffId}")
    public ResponseEntity<ApiResponse<StaffDTO>> getStaff(@PathVariable Long staffId) {
        return ResponseEntity.ok(ApiResponse.success(null,
                userManagementService.getStaff(staffId)));
    }


    @PutMapping("/staff/{staffId}")
    public ResponseEntity<ApiResponse<StaffDTO>> updateStaff(
            @PathVariable Long staffId,
            @Valid @RequestBody UpdateStaffRequest request,
            Authentication auth) {
        Role callerRole = Role.SUPER_USER;
        StaffDTO updated = userManagementService.updateStaff(staffId, request, callerRole);
        return ResponseEntity.ok(ApiResponse.success("Staff account updated.", updated));
    }

    @PatchMapping("/staff/{staffId}/role")
    public ResponseEntity<ApiResponse<Void>> changeRole(
            @PathVariable Long staffId,
            @RequestParam Role role,
            Authentication auth) {
        Long callerId = extractUserId(auth);
        userManagementService.changeStaffRole(staffId, role, callerId);
        return ResponseEntity.ok(ApiResponse.success(
                "Role updated to " + role.name() + ".", null));
    }

    @DeleteMapping("/staff/{staffId}")
    public ResponseEntity<ApiResponse<Void>> deleteStaff(
            @PathVariable Long staffId,
            Authentication auth) {
        Long callerId = extractUserId(auth);
        userManagementService.deleteStaff(staffId, callerId);
        return ResponseEntity.ok(ApiResponse.success("Staff account deactivated.", null));
    }

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
