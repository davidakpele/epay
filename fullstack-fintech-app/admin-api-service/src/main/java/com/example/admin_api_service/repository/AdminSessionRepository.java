package com.example.admin_api_service.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.example.admin_api_service.models.accessAndSecurity.AdminSession;

@Repository
public interface AdminSessionRepository extends JpaRepository<AdminSession, String> {
    Optional<AdminSession> findByAccessTokenAndIsActiveTrue(String accessToken);
    Optional<AdminSession> findByRefreshTokenAndIsActiveTrue(String refreshToken);
    Page<AdminSession> findAllByAdminUserId(Long adminUserId, Pageable pageable);
    List<AdminSession> findAllByAdminUserIdAndIsActiveTrue(Long adminUserId);
    List<AdminSession> findAllByAdminUserIdAndIsActiveTrueAndIdNot(Long adminUserId, String excludedId);
    List<AdminSession> findAllByIsActiveTrueAndExpiresAtBefore(LocalDateTime now);
}