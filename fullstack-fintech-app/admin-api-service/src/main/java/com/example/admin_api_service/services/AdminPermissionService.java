package com.example.admin_api_service.services;

import com.example.admin_api_service.Interfaces.IAdminPermissionService;
import com.example.admin_api_service.enums.PermissionAction;
import com.example.admin_api_service.enums.PermissionModule;
import com.example.admin_api_service.exceptions.ConflictException;
import com.example.admin_api_service.exceptions.ResourceNotFoundException;
import com.example.admin_api_service.models.accessAndSecurity.AdminPermission;
import com.example.admin_api_service.repository.AdminPermissionRepository;
import com.example.admin_api_service.repository.AdminRolePermissionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class AdminPermissionService implements IAdminPermissionService {

    private final AdminPermissionRepository adminPermissionRepository;
    private final AdminRolePermissionRepository adminRolePermissionRepository;

    public AdminPermissionService(AdminPermissionRepository adminPermissionRepository, AdminRolePermissionRepository adminRolePermissionRepository) {
        this.adminPermissionRepository = adminPermissionRepository;
        this.adminRolePermissionRepository = adminRolePermissionRepository;
    }

    @Override
    public AdminPermission createPermission(AdminPermission permission) {
        if (adminPermissionRepository.existsBySlug(permission.getSlug())) {
            throw new ConflictException("Permission with slug '" + permission.getSlug() + "' already exists");
        }
        permission.setActive(true);
        return adminPermissionRepository.save(permission);
    }

    @Override
    public AdminPermission updatePermission(String permissionId, AdminPermission updatedPermission) {
        AdminPermission existing = getPermissionById(permissionId);

        if (!existing.getSlug().equals(updatedPermission.getSlug())
                && adminPermissionRepository.existsBySlug(updatedPermission.getSlug())) {
            throw new ConflictException("Permission with slug '" + updatedPermission.getSlug() + "' already exists");
        }

        existing.setName(updatedPermission.getName());
        existing.setSlug(updatedPermission.getSlug());
        existing.setDescription(updatedPermission.getDescription());
        existing.setModule(updatedPermission.getModule());
        existing.setAction(updatedPermission.getAction());
        return adminPermissionRepository.save(existing);
    }

    @Override
    @Transactional(readOnly = true)
    public AdminPermission getPermissionById(String permissionId) {
        return adminPermissionRepository.findById(permissionId)
                .orElseThrow(() -> new ResourceNotFoundException("AdminPermission", "id", permissionId));
    }

    @Override
    @Transactional(readOnly = true)
    public AdminPermission getPermissionBySlug(String slug) {
        return adminPermissionRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("AdminPermission", "slug", slug));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AdminPermission> getAllPermissions(Pageable pageable) {
        return adminPermissionRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminPermission> getAllActivePermissions() {
        return adminPermissionRepository.findAllByIsActiveTrue();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminPermission> getPermissionsByModule(PermissionModule module) {
        return adminPermissionRepository.findAllByModule(module);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminPermission> getPermissionsByModuleAndAction(PermissionModule module, PermissionAction action) {
        return adminPermissionRepository.findAllByModuleAndAction(module, action);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminPermission> getPermissionsByRoleId(String roleId) {
        return adminRolePermissionRepository.findPermissionsByRoleId(roleId);
    }

    @Override
    public void activatePermission(String permissionId) {
        AdminPermission permission = getPermissionById(permissionId);
        permission.setActive(true);
        adminPermissionRepository.save(permission);
    }

    @Override
    public void deactivatePermission(String permissionId) {
        AdminPermission permission = getPermissionById(permissionId);
        permission.setActive(false);
        adminPermissionRepository.save(permission);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsBySlug(String slug) {
        return adminPermissionRepository.existsBySlug(slug);
    }
}
