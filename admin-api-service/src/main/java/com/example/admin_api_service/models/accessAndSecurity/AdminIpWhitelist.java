package com.example.admin_api_service.models.accessAndSecurity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

import com.example.admin_api_service.enums.IpWhitelistScope;
import com.example.admin_api_service.models.AdminUser;


@Entity
@Table(
    name = "admin_ip_whitelists",
    uniqueConstraints = @UniqueConstraint(columnNames = {"admin_user_id", "ip_address_or_cidr"})
)
public class AdminIpWhitelist {

    @Id
    @Column(name = "id", length = 36, nullable = false, updatable = false)
    private String id = UUID.randomUUID().toString();

    // Null = global rule; set = scoped to one admin
    @Column(name = "admin_user_id")
    private Long adminUserId;

    // Single IP (192.168.1.1) or CIDR range (10.0.0.0/24)
    @Column(name = "ip_address_or_cidr", length = 50, nullable = false)
    private String ipAddressOrCidr;

    @Column(name = "label", length = 100)
    private String label;

    @Enumerated(EnumType.STRING)
    @Column(name = "scope", length = 20, nullable = false)
    private IpWhitelistScope scope = IpWhitelistScope.INDIVIDUAL;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "created_by", length = 36)
    private String createdBy;

    @Column(name = "revoked_by", length = 36)
    private String revokedBy;

    @Column(name = "revoked_at")
    private LocalDateTime revokedAt;

    @Column(name = "revoked_reason", length = 255)
    private String revokedReason;

    @Column(name = "created_on", nullable = false, updatable = false)
    private LocalDateTime createdOn = LocalDateTime.now();

    @Column(name = "updated_on", nullable = false)
    private LocalDateTime updatedOn = LocalDateTime.now();

    // Navigation
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_user_id", insertable = false, updatable = false)
    private AdminUser adminUser;

    @PreUpdate
    public void preUpdate() {
        this.updatedOn = LocalDateTime.now();
    }

    public AdminIpWhitelist() {
    }

    public AdminIpWhitelist(String id, Long adminUserId, String ipAddressOrCidr, String label, IpWhitelistScope scope, boolean isActive, LocalDateTime expiresAt, String createdBy, String revokedBy, LocalDateTime revokedAt, String revokedReason, LocalDateTime createdOn, LocalDateTime updatedOn, AdminUser adminUser) {
        this.id = id;
        this.adminUserId = adminUserId;
        this.ipAddressOrCidr = ipAddressOrCidr;
        this.label = label;
        this.scope = scope;
        this.isActive = isActive;
        this.expiresAt = expiresAt;
        this.createdBy = createdBy;
        this.revokedBy = revokedBy;
        this.revokedAt = revokedAt;
        this.revokedReason = revokedReason;
        this.createdOn = createdOn;
        this.updatedOn = updatedOn;
        this.adminUser = adminUser;
    }

    // Getters & Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public Long getAdminUserId() { return adminUserId; }
    public void setAdminUserId(Long adminUserId) { this.adminUserId = adminUserId; }

    public String getIpAddressOrCidr() { return ipAddressOrCidr; }
    public void setIpAddressOrCidr(String ipAddressOrCidr) { this.ipAddressOrCidr = ipAddressOrCidr; }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }

    public IpWhitelistScope getScope() { return scope; }
    public void setScope(IpWhitelistScope scope) { this.scope = scope; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public String getRevokedBy() { return revokedBy; }
    public void setRevokedBy(String revokedBy) { this.revokedBy = revokedBy; }

    public LocalDateTime getRevokedAt() { return revokedAt; }
    public void setRevokedAt(LocalDateTime revokedAt) { this.revokedAt = revokedAt; }

    public String getRevokedReason() { return revokedReason; }
    public void setRevokedReason(String revokedReason) { this.revokedReason = revokedReason; }

    public LocalDateTime getCreatedOn() { return createdOn; }
    public void setCreatedOn(LocalDateTime createdOn) { this.createdOn = createdOn; }

    public LocalDateTime getUpdatedOn() { return updatedOn; }
    public void setUpdatedOn(LocalDateTime updatedOn) { this.updatedOn = updatedOn; }

    public AdminUser getAdminUser() { return adminUser; }
    public void setAdminUser(AdminUser adminUser) { this.adminUser = adminUser; }
}