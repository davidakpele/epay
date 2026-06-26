package com.example.admin_api_service.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.admin_api_service.enums.PermissionAction;
import com.example.admin_api_service.enums.PermissionModule;
import com.example.admin_api_service.models.accessAndSecurity.AdminPermission;

@Repository
public interface AdminPermissionRepository extends JpaRepository<AdminPermission, String> {
    Optional<AdminPermission> findBySlug(String slug);
    boolean existsBySlug(String slug);
    List<AdminPermission> findAllByIsActiveTrue();
    List<AdminPermission> findAllByModule(PermissionModule module);
    List<AdminPermission> findAllByModuleAndAction(PermissionModule module, PermissionAction action);
}