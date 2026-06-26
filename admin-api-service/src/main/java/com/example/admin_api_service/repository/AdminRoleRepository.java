package com.example.admin_api_service.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.example.admin_api_service.models.accessAndSecurity.AdminRole;

@Repository
public interface AdminRoleRepository extends JpaRepository<AdminRole, String> {
    
    Optional<AdminRole> findByName(String name);
    boolean existsByName(String name);
    List<AdminRole> findAllByIsActiveTrue();
}
