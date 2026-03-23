package com.example.admin_api_service.repository;

import java.time.LocalDateTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import com.example.admin_api_service.enums.IpWhitelistScope;
import com.example.admin_api_service.models.accessAndSecurity.AdminIpWhitelist;
import java.util.List;
import java.util.Optional;

@Repository
public interface AdminIpWhitelistRepository extends JpaRepository<AdminIpWhitelist, String> {

    @Query("SELECT a FROM AdminIpWhitelist a WHERE a.adminUserId = :adminUserId AND a.isActive = true")
    List<AdminIpWhitelist> findAllByAdminUserIdAndIsActiveTrue(String adminUserId);

    @Query("SELECT a FROM AdminIpWhitelist a WHERE a.scope = :scope AND a.isActive = true")
    List<AdminIpWhitelist> findAllByScopeAndIsActiveTrue(IpWhitelistScope scope);

    @Query("SELECT a FROM AdminIpWhitelist a WHERE a.isActive = true AND a.expiresAt < :now")
    List<AdminIpWhitelist> findAllByIsActiveTrueAndExpiresAtBefore(LocalDateTime now);

    @Query("SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END " +
           "FROM AdminIpWhitelist a " +
           "WHERE a.adminUserId = :adminUserId " +
           "AND a.ipAddressOrCidr = :ipAddressOrCidr " +
           "AND a.isActive = true")
    boolean existsByAdminUserIdAndIpAddressOrCidrAndIsActiveTrue(
            String adminUserId,
            String ipAddressOrCidr
    );

    @Query("SELECT a FROM AdminIpWhitelist a WHERE a.ipAddressOrCidr = :ip")
    Optional<AdminIpWhitelist> findByIpAddress(String ip);
}