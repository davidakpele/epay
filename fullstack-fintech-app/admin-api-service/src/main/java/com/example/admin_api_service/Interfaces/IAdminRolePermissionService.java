package com.example.admin_api_service.Interfaces;

import java.util.List;
import com.example.admin_api_service.models.accessAndSecurity.AdminRolePermission;

public interface IAdminRolePermissionService {
    AdminRolePermission grantPermissionToRole(String roleId, String permissionId, String grantedBy);
 
    void revokePermissionFromRole(String roleId, String permissionId);
 
    void revokeAllPermissionsFromRole(String roleId);
 
    List<AdminRolePermission> getPermissionsForRole(String roleId);
 
    boolean roleHasPermission(String roleId, String permissionSlug);
 
    boolean roleHasPermissionById(String roleId, String permissionId);
}
