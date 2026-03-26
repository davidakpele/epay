package com.example.admin_api_service.services;

import com.example.admin_api_service.Interfaces.IAdminSessionService;
import com.example.admin_api_service.enums.SessionTerminationReason;
import com.example.admin_api_service.exceptions.ResourceNotFoundException;
import com.example.admin_api_service.models.accessAndSecurity.AdminSession;
import com.example.admin_api_service.repository.AdminSessionRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class AdminSessionServiceImpl implements IAdminSessionService {

    private final AdminSessionRepository adminSessionRepository;

    public AdminSessionServiceImpl(AdminSessionRepository adminSessionRepository) {
        this.adminSessionRepository = adminSessionRepository;
    }

    @Override
    public AdminSession createSession(Long adminUserId, String accessToken, String refreshToken,
                                      String ipAddress, String userAgent, String deviceId,
                                      String geoLocation, int accessTokenTtlMinutes) {
        AdminSession session = new AdminSession();
        session.setAdminUserId(adminUserId);
        session.setAccessToken(accessToken);
        session.setRefreshToken(refreshToken);
        session.setIpAddress(ipAddress);
        session.setUserAgent(userAgent);
        session.setDeviceId(deviceId);
        session.setGeoLocation(geoLocation);
        session.setActive(true);
        session.setLastActivityAt(LocalDateTime.now());
        session.setExpiresAt(LocalDateTime.now().plusMinutes(accessTokenTtlMinutes));
        return adminSessionRepository.save(session);
    }

    @Override
    @Transactional(readOnly = true)
    public AdminSession getSessionById(String sessionId) {
        return adminSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("AdminSession", "id", sessionId));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AdminSession> getSessionByAccessToken(String accessToken) {
        return adminSessionRepository.findByAccessTokenAndIsActiveTrue(accessToken);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AdminSession> getSessionByRefreshToken(String refreshToken) {
        return adminSessionRepository.findByRefreshTokenAndIsActiveTrue(refreshToken);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AdminSession> getSessionsByAdminUser(Long adminUserId, Pageable pageable) {
        return adminSessionRepository.findAllByAdminUserId(adminUserId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminSession> getActiveSessionsByAdminUser(Long adminUserId) {
        return adminSessionRepository.findAllByAdminUserIdAndIsActiveTrue(adminUserId);
    }

    @Override
    public void revokeSession(String sessionId, String revokedBy, SessionTerminationReason reason) {
        AdminSession session = getSessionById(sessionId);
        session.setActive(false);
        session.setRevokedBy(revokedBy);
        session.setRevokedAt(LocalDateTime.now());
        session.setTerminationReason(reason);
        adminSessionRepository.save(session);
    }

    @Override
    public void revokeAllSessionsForAdminUser(Long adminUserId, SessionTerminationReason reason) {
        List<AdminSession> sessions = adminSessionRepository.findAllByAdminUserIdAndIsActiveTrue(adminUserId);
        sessions.forEach(session -> {
            session.setActive(false);
            session.setRevokedAt(LocalDateTime.now());
            session.setTerminationReason(reason);
        });
        adminSessionRepository.saveAll(sessions);
    }

    @Override
    public void revokeAllSessionsExcept(Long adminUserId, String currentSessionId, SessionTerminationReason reason) {
        List<AdminSession> sessions = adminSessionRepository
                .findAllByAdminUserIdAndIsActiveTrueAndIdNot(adminUserId, currentSessionId);
        sessions.forEach(session -> {
            session.setActive(false);
            session.setRevokedAt(LocalDateTime.now());
            session.setTerminationReason(reason);
        });
        adminSessionRepository.saveAll(sessions);
    }

    @Override
    public void updateLastActivity(String sessionId) {
        adminSessionRepository.findById(sessionId).ifPresent(session -> {
            session.setLastActivityAt(LocalDateTime.now());
            adminSessionRepository.save(session);
        });
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isSessionValid(String accessToken) {
        return adminSessionRepository.findByAccessTokenAndIsActiveTrue(accessToken)
                .map(session -> session.getExpiresAt().isAfter(LocalDateTime.now()))
                .orElse(false);
    }

    @Override
    @Scheduled(fixedDelay = 300000) // runs every 5 minutes
    public void expireOldSessions() {
        List<AdminSession> expiredSessions = adminSessionRepository
                .findAllByIsActiveTrueAndExpiresAtBefore(LocalDateTime.now());
        expiredSessions.forEach(session -> {
            session.setActive(false);
            session.setTerminationReason(SessionTerminationReason.EXPIRED);
        });
        if (!expiredSessions.isEmpty()) {
            adminSessionRepository.saveAll(expiredSessions);
        }
    }
}
