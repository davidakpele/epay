package com.example.admin_api_service.services;

import com.example.admin_api_service.Interfaces.IAdminRoleService;
import com.example.admin_api_service.exceptions.ConflictException;
import com.example.admin_api_service.exceptions.ResourceNotFoundException;
import com.example.admin_api_service.models.accessAndSecurity.AdminRole;
import com.example.admin_api_service.repository.AdminRoleRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class AdminRoleService implements IAdminRoleService {

    private final AdminRoleRepository adminRoleRepository;

    public AdminRoleService(AdminRoleRepository adminRoleRepository) {
        this.adminRoleRepository = adminRoleRepository;
    }

    @Override
    public AdminRole createRole(AdminRole role, String createdBy) {
        if (adminRoleRepository.existsByName(role.getName())) {
            throw new ConflictException("Role with name '" + role.getName() + "' already exists");
        }
        role.setCreatedBy(createdBy);
        role.setActive(true);
        role.setSystem(false);
        return adminRoleRepository.save(role);
    }

    @Override
    public AdminRole updateRole(String roleId, AdminRole updatedRole, String updatedBy) {
        AdminRole existing = getRoleById(roleId);

        if (existing.isSystem()) {
            throw new ConflictException("System roles cannot be modified");
        }

        if (!existing.getName().equals(updatedRole.getName())
                && adminRoleRepository.existsByName(updatedRole.getName())) {
            throw new ConflictException("Role with name '" + updatedRole.getName() + "' already exists");
        }

        existing.setName(updatedRole.getName());
        existing.setDescription(updatedRole.getDescription());
        existing.setUpdatedOn(LocalDateTime.now());
        return adminRoleRepository.save(existing);
    }

    @Override
    @Transactional(readOnly = true)
    public AdminRole getRoleById(String roleId) {
        return adminRoleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("AdminRole", "id", roleId));
    }

    @Override
    @Transactional(readOnly = true)
    public AdminRole getRoleByName(String name) {
        return adminRoleRepository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("AdminRole", "name", name));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AdminRole> getAllRoles(Pageable pageable) {
        return adminRoleRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminRole> getAllActiveRoles() {
        return adminRoleRepository.findAllByIsActiveTrue();
    }

    @Override
    public void activateRole(String roleId, String updatedBy) {
        AdminRole role = getRoleById(roleId);
        role.setActive(true);
        role.setUpdatedOn(LocalDateTime.now());
        adminRoleRepository.save(role);
    }

    @Override
    public void deactivateRole(String roleId, String updatedBy) {
        AdminRole role = getRoleById(roleId);
        if (role.isSystem()) {
            throw new ConflictException("System roles cannot be deactivated");
        }
        role.setActive(false);
        role.setUpdatedOn(LocalDateTime.now());
        adminRoleRepository.save(role);
    }

    @Override
    public void deleteRole(String roleId) {
        AdminRole role = getRoleById(roleId);
        if (role.isSystem()) {
            throw new ConflictException("System roles cannot be deleted");
        }
        if (!role.getAdminUsers().isEmpty()) {
            throw new ConflictException("Role is assigned to " + role.getAdminUsers().size() + " admin user(s) and cannot be deleted");
        }
        adminRoleRepository.delete(role);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByName(String name) {
        return adminRoleRepository.existsByName(name);
    }
}
