package com.example.admin_api_service.models.accessAndSecurity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;
import com.example.admin_api_service.enums.SessionTerminationReason;
import com.example.admin_api_service.models.AdminUser;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "admin_sessions")
public class AdminSession {

    @Id
    @Column(name = "id", length = 36, nullable = false, updatable = false)
    private String id = UUID.randomUUID().toString();

    @Column(name = "admin_user_id", nullable = false)
    private Long adminUserId;

    @Column(name = "access_token", length = 500, nullable = false, unique = true)
    private String accessToken;

    @Column(name = "refresh_token", length = 500)
    private String refreshToken;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", length = 255)
    private String userAgent;

    @Column(name = "device_id", length = 100)
    private String deviceId;

    @Column(name = "geo_location", length = 100)
    private String geoLocation;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @Enumerated(EnumType.STRING)
    @Column(name = "termination_reason", length = 30)
    private SessionTerminationReason terminationReason;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "last_activity_at")
    private LocalDateTime lastActivityAt;

    @Column(name = "revoked_at")
    private LocalDateTime revokedAt;

    @Column(name = "revoked_by", length = 36)
    private String revokedBy;

    @Column(name = "created_on", nullable = false, updatable = false)
    private LocalDateTime createdOn = LocalDateTime.now();

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_user_id", insertable = false, updatable = false)
    private AdminUser adminUser;

    public AdminSession() {
    }

    public AdminSession(String id, Long adminUserId, String accessToken, String refreshToken, String ipAddress, String userAgent, String deviceId, String geoLocation, boolean isActive, SessionTerminationReason terminationReason, LocalDateTime expiresAt, LocalDateTime lastActivityAt, LocalDateTime revokedAt, String revokedBy, LocalDateTime createdOn, AdminUser adminUser) {
        this.id = id;
        this.adminUserId = adminUserId;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.deviceId = deviceId;
        this.geoLocation = geoLocation;
        this.isActive = isActive;
        this.terminationReason = terminationReason;
        this.expiresAt = expiresAt;
        this.lastActivityAt = lastActivityAt;
        this.revokedAt = revokedAt;
        this.revokedBy = revokedBy;
        this.createdOn = createdOn;
        this.adminUser = adminUser;
    }

    // Getters & Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public Long getAdminUserId() { return adminUserId; }
    public void setAdminUserId(Long adminUserId) { this.adminUserId = adminUserId; }

    public String getAccessToken() { return accessToken; }
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }

    public String getRefreshToken() { return refreshToken; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public String getUserAgent() { return userAgent; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }

    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }

    public String getGeoLocation() { return geoLocation; }
    public void setGeoLocation(String geoLocation) { this.geoLocation = geoLocation; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public SessionTerminationReason getTerminationReason() { return terminationReason; }
    public void setTerminationReason(SessionTerminationReason terminationReason) { this.terminationReason = terminationReason; }

    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }

    public LocalDateTime getLastActivityAt() { return lastActivityAt; }
    public void setLastActivityAt(LocalDateTime lastActivityAt) { this.lastActivityAt = lastActivityAt; }

    public LocalDateTime getRevokedAt() { return revokedAt; }
    public void setRevokedAt(LocalDateTime revokedAt) { this.revokedAt = revokedAt; }

    public String getRevokedBy() { return revokedBy; }
    public void setRevokedBy(String revokedBy) { this.revokedBy = revokedBy; }

    public LocalDateTime getCreatedOn() { return createdOn; }
    public void setCreatedOn(LocalDateTime createdOn) { this.createdOn = createdOn; }

    public AdminUser getAdminUser() { return adminUser; }
    public void setAdminUser(AdminUser adminUser) { this.adminUser = adminUser; }
}