package com.example.admin_api_service.repository;

import java.time.LocalDateTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.example.admin_api_service.enums.IpWhitelistScope;
import com.example.admin_api_service.models.accessAndSecurity.AdminIpWhitelist;
import java.util.List;

@Repository
public interface AdminIpWhitelistRepository extends JpaRepository<AdminIpWhitelist, String> {
    List<AdminIpWhitelist> findAllByAdminUserIdAndIsActiveTrue(String adminUserId);
    List<AdminIpWhitelist> findAllByScopeAndIsActiveTrue(IpWhitelistScope scope);
    List<AdminIpWhitelist> findAllByIsActiveTrueAndExpiresAtBefore(LocalDateTime now);
    boolean existsByAdminUserIdAndIpAddressOrCidrAndIsActiveTrue(String adminUserId, String ipAddressOrCidr);
}