package com.example.admin_api_service.controllers;

import com.example.admin_api_service.Interfaces.IAdminPermissionService;
import com.example.admin_api_service.dto.ApiResponse;
import com.example.admin_api_service.enums.PermissionAction;
import com.example.admin_api_service.enums.PermissionModule;
import com.example.admin_api_service.models.accessAndSecurity.AdminPermission;
import com.example.admin_api_service.payloads.AdminPermissionRequest;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/admin/permissions")
public class AdminPermissionController {

    private final IAdminPermissionService adminPermissionService;

    public AdminPermissionController(IAdminPermissionService adminPermissionService) {
        this.adminPermissionService = adminPermissionService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AdminPermission>> createPermission(
            @Valid @RequestBody AdminPermissionRequest request) {

        AdminPermission permission = new AdminPermission();
        permission.setName(request.getName());
        permission.setSlug(request.getSlug());
        permission.setDescription(request.getDescription());
        permission.setModule(request.getModule());
        permission.setAction(request.getAction());

        AdminPermission created = adminPermissionService.createPermission(permission);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(created, "Permission created successfully"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<AdminPermission>>> getAllPermissions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Page<AdminPermission> permissions = adminPermissionService
                .getAllPermissions(PageRequest.of(page, size, Sort.by("module").ascending()));
        return ResponseEntity.ok(ApiResponse.success(permissions));
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<AdminPermission>>> getActivePermissions() {
        return ResponseEntity.ok(ApiResponse.success(adminPermissionService.getAllActivePermissions()));
    }

    @GetMapping("/{permissionId}")
    public ResponseEntity<ApiResponse<AdminPermission>> getPermissionById(
            @PathVariable String permissionId) {
        return ResponseEntity.ok(ApiResponse.success(
                adminPermissionService.getPermissionById(permissionId)));
    }

    @GetMapping("/slug/{slug}")
    public ResponseEntity<ApiResponse<AdminPermission>> getPermissionBySlug(
            @PathVariable String slug) {
        return ResponseEntity.ok(ApiResponse.success(
                adminPermissionService.getPermissionBySlug(slug)));
    }

    @GetMapping("/module/{module}")
    public ResponseEntity<ApiResponse<List<AdminPermission>>> getPermissionsByModule(
            @PathVariable PermissionModule module) {
        return ResponseEntity.ok(ApiResponse.success(
                adminPermissionService.getPermissionsByModule(module)));
    }

    @GetMapping("/module/{module}/action/{action}")
    public ResponseEntity<ApiResponse<List<AdminPermission>>> getPermissionsByModuleAndAction(
            @PathVariable PermissionModule module,
            @PathVariable PermissionAction action) {
        return ResponseEntity.ok(ApiResponse.success(
                adminPermissionService.getPermissionsByModuleAndAction(module, action)));
    }

    @GetMapping("/role/{roleId}")
    public ResponseEntity<ApiResponse<List<AdminPermission>>> getPermissionsByRole(
            @PathVariable String roleId) {
        return ResponseEntity.ok(ApiResponse.success(
                adminPermissionService.getPermissionsByRoleId(roleId)));
    }

    @PutMapping("/{permissionId}")
    public ResponseEntity<ApiResponse<AdminPermission>> updatePermission(
            @PathVariable String permissionId,
            @Valid @RequestBody AdminPermissionRequest request) {

        AdminPermission permission = new AdminPermission();
        permission.setName(request.getName());
        permission.setSlug(request.getSlug());
        permission.setDescription(request.getDescription());
        permission.setModule(request.getModule());
        permission.setAction(request.getAction());

        return ResponseEntity.ok(ApiResponse.success(
                adminPermissionService.updatePermission(permissionId, permission),
                "Permission updated"));
    }

    @PatchMapping("/{permissionId}/activate")
    public ResponseEntity<ApiResponse<Void>> activatePermission(@PathVariable String permissionId) {
        adminPermissionService.activatePermission(permissionId);
        return ResponseEntity.ok(ApiResponse.success(null, "Permission activated"));
    }

    @PatchMapping("/{permissionId}/deactivate")
    public ResponseEntity<ApiResponse<Void>> deactivatePermission(@PathVariable String permissionId) {
        adminPermissionService.deactivatePermission(permissionId);
        return ResponseEntity.ok(ApiResponse.success(null, "Permission deactivated"));
    }
}
