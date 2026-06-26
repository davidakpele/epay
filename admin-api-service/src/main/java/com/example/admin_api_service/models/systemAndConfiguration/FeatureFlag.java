package com.example.admin_api_service.models.systemAndConfiguration;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

import com.example.admin_api_service.enums.FeatureFlagRolloutStrategy;

@Entity
@Table(name = "feature_flags")
public class FeatureFlag {

    @Id
    @Column(name = "id", length = 36, nullable = false, updatable = false)
    private String id = UUID.randomUUID().toString();

    // Unique slug e.g. "enable_ussd_payments", "new_kyc_flow"
    @Column(name = "flag_key", length = 200, nullable = false, unique = true)
    private String flagKey;

    @Column(name = "name", length = 200, nullable = false)
    private String name;

    @Column(name = "description", length = 500)
    private String description;

    // Service this flag belongs to
    @Column(name = "service_name", length = 100)
    private String serviceName;

    @Column(name = "is_enabled", nullable = false)
    private boolean isEnabled = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "rollout_strategy", length = 30, nullable = false)
    private FeatureFlagRolloutStrategy rolloutStrategy = FeatureFlagRolloutStrategy.ALL_OR_NOTHING;

    // Percentage of users this flag is active for (0–100) when strategy = PERCENTAGE_ROLLOUT
    @Column(name = "rollout_percentage")
    private Integer rolloutPercentage;

    // Specific user IDs targeted stored as JSON array (strategy = USER_WHITELIST)
    @Column(name = "whitelisted_user_ids", columnDefinition = "json")
    private String whitelistedUserIds;

    // Specific KYC tiers this flag applies to stored as JSON array
    @Column(name = "applicable_tiers", columnDefinition = "json")
    private String applicableTiers;

    // Specific channels this flag applies to stored as JSON array
    @Column(name = "applicable_channels", columnDefinition = "json")
    private String applicableChannels;

    // Environment this flag is active in: ALL, PRODUCTION, STAGING
    @Column(name = "environment", length = 20)
    private String environment;

    @Column(name = "enabled_at")
    private LocalDateTime enabledAt;

    @Column(name = "disabled_at")
    private LocalDateTime disabledAt;

    // Auto-disable after this date — null means no expiry
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "updated_by", length = 36)
    private String updatedBy;

    @Column(name = "update_reason", length = 500)
    private String updateReason;

    @Column(name = "created_on", nullable = false, updatable = false)
    private LocalDateTime createdOn = LocalDateTime.now();

    @Column(name = "updated_on", nullable = false)
    private LocalDateTime updatedOn = LocalDateTime.now();

    @PreUpdate
    public void preUpdate() { this.updatedOn = LocalDateTime.now(); }

    public FeatureFlag() {
    }

    public FeatureFlag(String id, String flagKey, String name, String description, String serviceName, boolean isEnabled, FeatureFlagRolloutStrategy rolloutStrategy, Integer rolloutPercentage, String whitelistedUserIds, String applicableTiers, String applicableChannels, String environment, LocalDateTime enabledAt, LocalDateTime disabledAt, LocalDateTime expiresAt, String updatedBy, String updateReason, LocalDateTime createdOn, LocalDateTime updatedOn) {
        this.id = id;
        this.flagKey = flagKey;
        this.name = name;
        this.description = description;
        this.serviceName = serviceName;
        this.isEnabled = isEnabled;
        this.rolloutStrategy = rolloutStrategy;
        this.rolloutPercentage = rolloutPercentage;
        this.whitelistedUserIds = whitelistedUserIds;
        this.applicableTiers = applicableTiers;
        this.applicableChannels = applicableChannels;
        this.environment = environment;
        this.enabledAt = enabledAt;
        this.disabledAt = disabledAt;
        this.expiresAt = expiresAt;
        this.updatedBy = updatedBy;
        this.updateReason = updateReason;
        this.createdOn = createdOn;
        this.updatedOn = updatedOn;
    }

    // Getters & Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getFlagKey() { return flagKey; }
    public void setFlagKey(String flagKey) { this.flagKey = flagKey; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getServiceName() { return serviceName; }
    public void setServiceName(String serviceName) { this.serviceName = serviceName; }

    public boolean isEnabled() { return isEnabled; }
    public void setEnabled(boolean enabled) { isEnabled = enabled; }

    public FeatureFlagRolloutStrategy getRolloutStrategy() { return rolloutStrategy; }
    public void setRolloutStrategy(FeatureFlagRolloutStrategy rolloutStrategy) { this.rolloutStrategy = rolloutStrategy; }

    public Integer getRolloutPercentage() { return rolloutPercentage; }
    public void setRolloutPercentage(Integer rolloutPercentage) { this.rolloutPercentage = rolloutPercentage; }

    public String getWhitelistedUserIds() { return whitelistedUserIds; }
    public void setWhitelistedUserIds(String whitelistedUserIds) { this.whitelistedUserIds = whitelistedUserIds; }

    public String getApplicableTiers() { return applicableTiers; }
    public void setApplicableTiers(String applicableTiers) { this.applicableTiers = applicableTiers; }

    public String getApplicableChannels() { return applicableChannels; }
    public void setApplicableChannels(String applicableChannels) { this.applicableChannels = applicableChannels; }

    public String getEnvironment() { return environment; }
    public void setEnvironment(String environment) { this.environment = environment; }

    public LocalDateTime getEnabledAt() { return enabledAt; }
    public void setEnabledAt(LocalDateTime enabledAt) { this.enabledAt = enabledAt; }

    public LocalDateTime getDisabledAt() { return disabledAt; }
    public void setDisabledAt(LocalDateTime disabledAt) { this.disabledAt = disabledAt; }

    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }

    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }

    public String getUpdateReason() { return updateReason; }
    public void setUpdateReason(String updateReason) { this.updateReason = updateReason; }

    public LocalDateTime getCreatedOn() { return createdOn; }
    public void setCreatedOn(LocalDateTime createdOn) { this.createdOn = createdOn; }

    public LocalDateTime getUpdatedOn() { return updatedOn; }
    public void setUpdatedOn(LocalDateTime updatedOn) { this.updatedOn = updatedOn; }
}
