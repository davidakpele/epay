package com.example.admin_api_service.Interfaces;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.example.admin_api_service.enums.PermissionAction;
import com.example.admin_api_service.enums.PermissionModule;
import com.example.admin_api_service.models.accessAndSecurity.AdminPermission;

public interface IAdminPermissionService {
       AdminPermission createPermission(AdminPermission permission);
 
    AdminPermission updatePermission(String permissionId, AdminPermission updatedPermission);
 
    AdminPermission getPermissionById(String permissionId);
 
    AdminPermission getPermissionBySlug(String slug);
 
    Page<AdminPermission> getAllPermissions(Pageable pageable);
 
    List<AdminPermission> getAllActivePermissions();
 
    List<AdminPermission> getPermissionsByModule(PermissionModule module);
 
    List<AdminPermission> getPermissionsByModuleAndAction(PermissionModule module, PermissionAction action);
 
    List<AdminPermission> getPermissionsByRoleId(String roleId);
 
    void activatePermission(String permissionId);
 
    void deactivatePermission(String permissionId);
 
    boolean existsBySlug(String slug);
}
