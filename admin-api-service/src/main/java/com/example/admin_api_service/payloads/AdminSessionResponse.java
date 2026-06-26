package com.example.admin_api_service.payloads;

import java.time.LocalDateTime;
import com.example.admin_api_service.enums.SessionTerminationReason;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminSessionResponse {
    private String id;
    private String adminUserId;
    private String ipAddress;
    private String userAgent;
    private String deviceId;
    private String geoLocation;
    private boolean isActive;
    private SessionTerminationReason terminationReason;
    private LocalDateTime expiresAt;
    private LocalDateTime lastActivityAt;
    private LocalDateTime createdOn;
 
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getAdminUserId() { return adminUserId; }
    public void setAdminUserId(String adminUserId) { this.adminUserId = adminUserId; }
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
    public LocalDateTime getCreatedOn() { return createdOn; }
    public void setCreatedOn(LocalDateTime createdOn) { this.createdOn = createdOn; }
}
