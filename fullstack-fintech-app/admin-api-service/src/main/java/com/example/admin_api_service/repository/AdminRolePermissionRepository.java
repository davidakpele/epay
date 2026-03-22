package com.example.admin_api_service.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.admin_api_service.models.accessAndSecurity.AdminPermission;
import com.example.admin_api_service.models.accessAndSecurity.AdminRolePermission;

@Repository
public interface AdminRolePermissionRepository extends JpaRepository<AdminRolePermission, String> {
    List<AdminRolePermission> findAllByRoleId(String roleId);
    Optional<AdminRolePermission> findByRoleIdAndPermissionId(String roleId, String permissionId);
    boolean existsByRoleIdAndPermissionId(String roleId, String permissionId);
 
    @Query("SELECT rp.permission FROM AdminRolePermission rp WHERE rp.roleId = :roleId")
    List<AdminPermission> findPermissionsByRoleId(@Param("roleId") String roleId);
 
    @Query("SELECT COUNT(rp) > 0 FROM AdminRolePermission rp " +
           "JOIN AdminPermission p ON rp.permissionId = p.id " +
           "WHERE rp.roleId = :roleId AND p.slug = :slug")
    boolean existsByRoleIdAndPermissionSlug(@Param("roleId") String roleId, @Param("slug") String slug);
 
    @Modifying
    @Query("DELETE FROM AdminRolePermission rp WHERE rp.roleId = :roleId")
    void deleteAllByRoleId(@Param("roleId") String roleId);
}