package com.example.admin_api_service.models.accessAndSecurity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

import com.example.admin_api_service.models.AdminUser;

@Entity
@Table(name = "admin_password_histories")
public class AdminPasswordHistory {

    @Id
    @Column(name = "id", length = 36, nullable = false, updatable = false)
    private String id = UUID.randomUUID().toString();

    @Column(name = "admin_user_id", length = 36, nullable = false)
    private String adminUserId;

    // Bcrypt / Argon2 hash — never plaintext
    @Column(name = "password_hash", length = 500, nullable = false)
    private String passwordHash;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", length = 255)
    private String userAgent;

    // e.g. VOLUNTARY_CHANGE, FORCED_RESET, ADMIN_RESET, EXPIRY
    @Column(name = "change_reason", length = 30)
    private String changeReason;

    @Column(name = "changed_by", length = 36)
    private String changedBy;

    @Column(name = "created_on", nullable = false, updatable = false)
    private LocalDateTime createdOn = LocalDateTime.now();

    // Navigation
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_user_id", insertable = false, updatable = false)
    private AdminUser adminUser;

    public AdminPasswordHistory() {
    }

    public AdminPasswordHistory(String id, String adminUserId, String passwordHash, String ipAddress, String userAgent, String changeReason, String changedBy, LocalDateTime createdOn, AdminUser adminUser) {
        this.id = id;
        this.adminUserId = adminUserId;
        this.passwordHash = passwordHash;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.changeReason = changeReason;
        this.changedBy = changedBy;
        this.createdOn = createdOn;
        this.adminUser = adminUser;
    }

    // Getters & Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getAdminUserId() { return adminUserId; }
    public void setAdminUserId(String adminUserId) { this.adminUserId = adminUserId; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public String getUserAgent() { return userAgent; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }

    public String getChangeReason() { return changeReason; }
    public void setChangeReason(String changeReason) { this.changeReason = changeReason; }

    public String getChangedBy() { return changedBy; }
    public void setChangedBy(String changedBy) { this.changedBy = changedBy; }

    public LocalDateTime getCreatedOn() { return createdOn; }
    public void setCreatedOn(LocalDateTime createdOn) { this.createdOn = createdOn; }

    public AdminUser getAdminUser() { return adminUser; }
    public void setAdminUser(AdminUser adminUser) { this.adminUser = adminUser; }
}
