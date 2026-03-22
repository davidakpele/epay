package com.example.admin_api_service.Interfaces;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.example.admin_api_service.enums.SessionTerminationReason;
import com.example.admin_api_service.models.accessAndSecurity.AdminSession;

public interface IAdminSessionService {
    AdminSession createSession(String adminUserId, String accessToken, String refreshToken,
                               String ipAddress, String userAgent, String deviceId,
                               String geoLocation, int accessTokenTtlMinutes);
 
    AdminSession getSessionById(String sessionId);
 
    Optional<AdminSession> getSessionByAccessToken(String accessToken);
 
    Optional<AdminSession> getSessionByRefreshToken(String refreshToken);
 
    Page<AdminSession> getSessionsByAdminUser(String adminUserId, Pageable pageable);
 
    List<AdminSession> getActiveSessionsByAdminUser(String adminUserId);
 
    void revokeSession(String sessionId, String revokedBy, SessionTerminationReason reason);
 
    void revokeAllSessionsForAdminUser(String adminUserId, SessionTerminationReason reason);
 
    void revokeAllSessionsExcept(String adminUserId, String currentSessionId, SessionTerminationReason reason);
 
    void updateLastActivity(String sessionId);
 
    boolean isSessionValid(String accessToken);
 
    void expireOldSessions();
}
