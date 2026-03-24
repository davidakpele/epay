package com.example.admin_api_service.models.accessAndSecurity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;
import com.example.admin_api_service.enums.TwoFactorMethod;
import com.example.admin_api_service.models.AdminUser;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "admin_two_factor_auth")
public class AdminTwoFactorAuth {

    @Id
    @Column(name = "id", length = 36, nullable = false, updatable = false)
    private String id = UUID.randomUUID().toString();

    @Column(name = "admin_user_id", nullable = false)
    private Long adminUserId;

    @Enumerated(EnumType.STRING)
    @Column(name = "method", length = 20, nullable = false)
    private TwoFactorMethod method;

    // Encrypted TOTP secret or masked phone/email — never plaintext
    @Column(name = "secret", length = 500)
    private String secret;

    @Column(name = "is_enabled", nullable = false)
    private boolean isEnabled = false;

    @Column(name = "is_verified", nullable = false)
    private boolean isVerified = false;

    // Hashed backup codes stored as JSON array
    @Column(name = "backup_codes", columnDefinition = "json")
    private String backupCodes;

    @Column(name = "backup_codes_remaining_count", nullable = false)
    private int backupCodesRemainingCount = 0;

    @Column(name = "enabled_at")
    private LocalDateTime enabledAt;

    @Column(name = "last_used_at")
    private LocalDateTime lastUsedAt;

    @Column(name = "failed_attempts", nullable = false)
    private int failedAttempts = 0;

    @Column(name = "locked_until")
    private LocalDateTime lockedUntil;

    @Column(name = "created_on", nullable = false, updatable = false)
    private LocalDateTime createdOn = LocalDateTime.now();

    @Column(name = "updated_on", nullable = false)
    private LocalDateTime updatedOn = LocalDateTime.now();

    // Navigation
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_user_id", insertable = false, updatable = false)
    private AdminUser adminUser;

    @PreUpdate
    public void preUpdate() {
        this.updatedOn = LocalDateTime.now();
    }

    public AdminTwoFactorAuth() {
    }

    public AdminTwoFactorAuth(String id, Long adminUserId, TwoFactorMethod method, String secret, boolean isEnabled, boolean isVerified, String backupCodes, int backupCodesRemainingCount, LocalDateTime enabledAt, LocalDateTime lastUsedAt, int failedAttempts, LocalDateTime lockedUntil, LocalDateTime createdOn, LocalDateTime updatedOn, AdminUser adminUser) {
        this.id = id;
        this.adminUserId = adminUserId;
        this.method = method;
        this.secret = secret;
        this.isEnabled = isEnabled;
        this.isVerified = isVerified;
        this.backupCodes = backupCodes;
        this.backupCodesRemainingCount = backupCodesRemainingCount;
        this.enabledAt = enabledAt;
        this.lastUsedAt = lastUsedAt;
        this.failedAttempts = failedAttempts;
        this.lockedUntil = lockedUntil;
        this.createdOn = createdOn;
        this.updatedOn = updatedOn;
        this.adminUser = adminUser;
    }

    // Getters & Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public Long getAdminUserId() { return adminUserId; }
    public void setAdminUserId(Long adminUserId) { this.adminUserId = adminUserId; }

    public TwoFactorMethod getMethod() { return method; }
    public void setMethod(TwoFactorMethod method) { this.method = method; }

    public String getSecret() { return secret; }
    public void setSecret(String secret) { this.secret = secret; }

    public boolean isEnabled() { return isEnabled; }
    public void setEnabled(boolean enabled) { isEnabled = enabled; }

    public boolean isVerified() { return isVerified; }
    public void setVerified(boolean verified) { isVerified = verified; }

    public String getBackupCodes() { return backupCodes; }
    public void setBackupCodes(String backupCodes) { this.backupCodes = backupCodes; }

    public int getBackupCodesRemainingCount() { return backupCodesRemainingCount; }
    public void setBackupCodesRemainingCount(int backupCodesRemainingCount) { this.backupCodesRemainingCount = backupCodesRemainingCount; }

    public LocalDateTime getEnabledAt() { return enabledAt; }
    public void setEnabledAt(LocalDateTime enabledAt) { this.enabledAt = enabledAt; }

    public LocalDateTime getLastUsedAt() { return lastUsedAt; }
    public void setLastUsedAt(LocalDateTime lastUsedAt) { this.lastUsedAt = lastUsedAt; }

    public int getFailedAttempts() { return failedAttempts; }
    public void setFailedAttempts(int failedAttempts) { this.failedAttempts = failedAttempts; }

    public LocalDateTime getLockedUntil() { return lockedUntil; }
    public void setLockedUntil(LocalDateTime lockedUntil) { this.lockedUntil = lockedUntil; }

    public LocalDateTime getCreatedOn() { return createdOn; }
    public void setCreatedOn(LocalDateTime createdOn) { this.createdOn = createdOn; }

    public LocalDateTime getUpdatedOn() { return updatedOn; }
    public void setUpdatedOn(LocalDateTime updatedOn) { this.updatedOn = updatedOn; }

    public AdminUser getAdminUser() { return adminUser; }
    public void setAdminUser(AdminUser adminUser) { this.adminUser = adminUser; }
}