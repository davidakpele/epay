package com.example.admin_api_service.controllers;

import com.example.admin_api_service.Interfaces.IAdminRolePermissionService;
import com.example.admin_api_service.Interfaces.IAdminRoleService;
import com.example.admin_api_service.dto.ApiResponse;
import com.example.admin_api_service.models.accessAndSecurity.AdminRole;
import com.example.admin_api_service.models.accessAndSecurity.AdminRolePermission;
import com.example.admin_api_service.payloads.AdminRoleRequest;
import com.example.admin_api_service.payloads.GrantPermissionRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/roles")
public class AdminRoleController {

    private final IAdminRoleService adminRoleService;
    private final IAdminRolePermissionService rolePermissionService;

    public AdminRoleController(IAdminRoleService adminRoleService,
                                IAdminRolePermissionService rolePermissionService) {
        this.adminRoleService = adminRoleService;
        this.rolePermissionService = rolePermissionService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AdminRole>> createRole(
            @Valid @RequestBody AdminRoleRequest request,
            @AuthenticationPrincipal String adminId) {

        AdminRole role = new AdminRole();
        role.setName(request.getName());
        role.setDescription(request.getDescription());

        AdminRole created = adminRoleService.createRole(role, adminId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(created, "Role created successfully"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<AdminRole>>> getAllRoles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdOn") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {

        Sort sort = direction.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Page<AdminRole> roles = adminRoleService.getAllRoles(PageRequest.of(page, size, sort));
        return ResponseEntity.ok(ApiResponse.success(roles));
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<AdminRole>>> getActiveRoles() {
        return ResponseEntity.ok(ApiResponse.success(adminRoleService.getAllActiveRoles()));
    }

    @GetMapping("/{roleId}")
    public ResponseEntity<ApiResponse<AdminRole>> getRoleById(@PathVariable String roleId) {
        return ResponseEntity.ok(ApiResponse.success(adminRoleService.getRoleById(roleId)));
    }

    @PutMapping("/{roleId}")
    public ResponseEntity<ApiResponse<AdminRole>> updateRole(
            @PathVariable String roleId,
            @Valid @RequestBody AdminRoleRequest request,
            @AuthenticationPrincipal String adminId) {

        AdminRole role = new AdminRole();
        role.setName(request.getName());
        role.setDescription(request.getDescription());

        AdminRole updated = adminRoleService.updateRole(roleId, role, adminId);
        return ResponseEntity.ok(ApiResponse.success(updated, "Role updated successfully"));
    }

    @PatchMapping("/{roleId}/activate")
    public ResponseEntity<ApiResponse<Void>> activateRole(
            @PathVariable String roleId,
            @AuthenticationPrincipal String adminId) {

        adminRoleService.activateRole(roleId, adminId);
        return ResponseEntity.ok(ApiResponse.success(null, "Role activated"));
    }

    @PatchMapping("/{roleId}/deactivate")
    public ResponseEntity<ApiResponse<Void>> deactivateRole(
            @PathVariable String roleId,
            @AuthenticationPrincipal String adminId) {

        adminRoleService.deactivateRole(roleId, adminId);
        return ResponseEntity.ok(ApiResponse.success(null, "Role deactivated"));
    }

    @DeleteMapping("/{roleId}")
    public ResponseEntity<ApiResponse<Void>> deleteRole(@PathVariable String roleId) {
        adminRoleService.deleteRole(roleId);
        return ResponseEntity.ok(ApiResponse.success(null, "Role deleted"));
    }

    // ── Role Permission management ─────────────────────────────────────────────

    @PostMapping("/{roleId}/permissions")
    public ResponseEntity<ApiResponse<AdminRolePermission>> grantPermission(
            @PathVariable String roleId,
            @Valid @RequestBody GrantPermissionRequest request,
            @AuthenticationPrincipal String adminId) {

        AdminRolePermission granted = rolePermissionService
                .grantPermissionToRole(roleId, request.getPermissionId(), adminId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(granted, "Permission granted to role"));
    }

    @GetMapping("/{roleId}/permissions")
    public ResponseEntity<ApiResponse<List<AdminRolePermission>>> getRolePermissions(
            @PathVariable String roleId) {

        List<AdminRolePermission> permissions = rolePermissionService.getPermissionsForRole(roleId);
        return ResponseEntity.ok(ApiResponse.success(permissions));
    }

    @DeleteMapping("/{roleId}/permissions/{permissionId}")
    public ResponseEntity<ApiResponse<Void>> revokePermission(
            @PathVariable String roleId,
            @PathVariable String permissionId) {

        rolePermissionService.revokePermissionFromRole(roleId, permissionId);
        return ResponseEntity.ok(ApiResponse.success(null, "Permission revoked from role"));
    }

    @DeleteMapping("/{roleId}/permissions")
    public ResponseEntity<ApiResponse<Void>> revokeAllPermissions(@PathVariable String roleId) {
        rolePermissionService.revokeAllPermissionsFromRole(roleId);
        return ResponseEntity.ok(ApiResponse.success(null, "All permissions revoked from role"));
    }
}