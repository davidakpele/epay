package com.example.admin_api_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.example.admin_api_service.models.accessAndSecurity.AdminRolePermission;

@Repository
public interface AdminRolePermissionRepository extends JpaRepository<AdminRolePermission, String> {

}