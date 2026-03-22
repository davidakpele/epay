package com.example.admin_api_service.services;

import com.example.admin_api_service.Interfaces.IAdminRolePermissionService;
import com.example.admin_api_service.exceptions.ConflictException;
import com.example.admin_api_service.exceptions.ResourceNotFoundException;
import com.example.admin_api_service.models.accessAndSecurity.AdminRolePermission;
import com.example.admin_api_service.repository.AdminRolePermissionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class AdminRolePermissionService implements IAdminRolePermissionService {

    private final AdminRolePermissionRepository adminRolePermissionRepository;
    private final AdminRoleService adminRoleService;
    private final AdminPermissionService adminPermissionService;

    public AdminRolePermissionService(AdminRolePermissionRepository adminRolePermissionRepository,
                                          AdminRoleService adminRoleService,
                                          AdminPermissionService adminPermissionService) {
        this.adminRolePermissionRepository = adminRolePermissionRepository;
        this.adminRoleService = adminRoleService;
        this.adminPermissionService = adminPermissionService;
    }

    @Override
    public AdminRolePermission grantPermissionToRole(String roleId, String permissionId, String grantedBy) {
        // Validate both exist
        adminRoleService.getRoleById(roleId);
        adminPermissionService.getPermissionById(permissionId);

        if (adminRolePermissionRepository.existsByRoleIdAndPermissionId(roleId, permissionId)) {
            throw new ConflictException("Permission is already granted to this role");
        }

        AdminRolePermission rolePermission = new AdminRolePermission();
        rolePermission.setRoleId(roleId);
        rolePermission.setPermissionId(permissionId);
        rolePermission.setGrantedBy(grantedBy);
        return adminRolePermissionRepository.save(rolePermission);
    }

    @Override
    public void revokePermissionFromRole(String roleId, String permissionId) {
        AdminRolePermission rolePermission = adminRolePermissionRepository
                .findByRoleIdAndPermissionId(roleId, permissionId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "AdminRolePermission", "roleId+permissionId", roleId + "+" + permissionId));
        adminRolePermissionRepository.delete(rolePermission);
    }

    @Override
    public void revokeAllPermissionsFromRole(String roleId) {
        adminRoleService.getRoleById(roleId);
        adminRolePermissionRepository.deleteAllByRoleId(roleId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminRolePermission> getPermissionsForRole(String roleId) {
        adminRoleService.getRoleById(roleId);
        return adminRolePermissionRepository.findAllByRoleId(roleId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean roleHasPermission(String roleId, String permissionSlug) {
        return adminRolePermissionRepository.existsByRoleIdAndPermissionSlug(roleId, permissionSlug);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean roleHasPermissionById(String roleId, String permissionId) {
        return adminRolePermissionRepository.existsByRoleIdAndPermissionId(roleId, permissionId);
    }
}
