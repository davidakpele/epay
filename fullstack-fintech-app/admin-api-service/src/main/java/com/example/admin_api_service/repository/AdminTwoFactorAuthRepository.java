package com.example.admin_api_service.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.admin_api_service.enums.TwoFactorMethod;
import com.example.admin_api_service.models.accessAndSecurity.AdminTwoFactorAuth;

@Repository
public interface AdminTwoFactorAuthRepository extends JpaRepository<AdminTwoFactorAuth, String> {
    Optional<AdminTwoFactorAuth> findByAdminUserIdAndMethod(String adminUserId, TwoFactorMethod method);
    Optional<AdminTwoFactorAuth> findByAdminUserIdAndMethodAndIsEnabledTrue(String adminUserId, TwoFactorMethod method);
    List<AdminTwoFactorAuth> findByAdminUserIdAndIsEnabledTrue(String adminUserId);
    List<AdminTwoFactorAuth> findAllByAdminUserId(String adminUserId);
}
