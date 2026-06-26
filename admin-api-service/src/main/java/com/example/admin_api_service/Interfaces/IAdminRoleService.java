package com.example.admin_api_service.Interfaces;

import com.example.admin_api_service.models.accessAndSecurity.AdminRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface IAdminRoleService {
    AdminRole createRole(AdminRole role, String createdBy);
 
    AdminRole updateRole(String roleId, AdminRole updatedRole, String updatedBy);
 
    AdminRole getRoleById(String roleId);
 
    AdminRole getRoleByName(String name);
 
    Page<AdminRole> getAllRoles(Pageable pageable);
 
    List<AdminRole> getAllActiveRoles();
 
    void activateRole(String roleId, String updatedBy);
 
    void deactivateRole(String roleId, String updatedBy);
 
    void deleteRole(String roleId);
 
    boolean existsByName(String name);
}
