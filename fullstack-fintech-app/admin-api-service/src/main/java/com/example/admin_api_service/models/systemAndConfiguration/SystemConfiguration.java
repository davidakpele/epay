package com.example.admin_api_service.models.systemAndConfiguration;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

import com.example.admin_api_service.enums.SystemConfigDataType;
import com.example.admin_api_service.enums.SystemConfigScope;

@Entity
@Table(name = "system_configurations")
public class SystemConfiguration {

    @Id
    @Column(name = "id", length = 36, nullable = false, updatable = false)
    private String id = UUID.randomUUID().toString();

    // Unique dot-notation key e.g. "payment.transfer.max_retries"
    @Column(name = "config_key", length = 200, nullable = false, unique = true)
    private String configKey;

    @Column(name = "config_value", columnDefinition = "text", nullable = false)
    private String configValue;

    // Previous value — kept for rollback and audit
    @Column(name = "previous_value", columnDefinition = "text")
    private String previousValue;

    @Enumerated(EnumType.STRING)
    @Column(name = "data_type", length = 20, nullable = false)
    private SystemConfigDataType dataType;

    @Enumerated(EnumType.STRING)
    @Column(name = "scope", length = 20, nullable = false)
    private SystemConfigScope scope = SystemConfigScope.GLOBAL;

    // Service or module this config belongs to e.g. "payment-service", "kyc-service"
    @Column(name = "service_name", length = 100)
    private String serviceName;

    @Column(name = "description", length = 500)
    private String description;

    // Whether this config value is safe to expose in non-sensitive logs/responses
    @Column(name = "is_sensitive", nullable = false)
    private boolean isSensitive = false;

    // Whether this config can be changed at runtime without a service restart
    @Column(name = "is_hot_reload", nullable = false)
    private boolean isHotReload = false;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    // Allowed values stored as JSON array — null means any value is accepted
    @Column(name = "allowed_values", columnDefinition = "json")
    private String allowedValues;

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

    public SystemConfiguration() {
    }

    public SystemConfiguration(String id, String configKey, String configValue, String previousValue, SystemConfigDataType dataType, SystemConfigScope scope, String serviceName, String description, boolean isSensitive, boolean isHotReload, boolean isActive, String allowedValues, String updatedBy, String updateReason, LocalDateTime createdOn, LocalDateTime updatedOn) {
        this.id = id;
        this.configKey = configKey;
        this.configValue = configValue;
        this.previousValue = previousValue;
        this.dataType = dataType;
        this.scope = scope;
        this.serviceName = serviceName;
        this.description = description;
        this.isSensitive = isSensitive;
        this.isHotReload = isHotReload;
        this.isActive = isActive;
        this.allowedValues = allowedValues;
        this.updatedBy = updatedBy;
        this.updateReason = updateReason;
        this.createdOn = createdOn;
        this.updatedOn = updatedOn;
    }

    // Getters & Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getConfigKey() { return configKey; }
    public void setConfigKey(String configKey) { this.configKey = configKey; }

    public String getConfigValue() { return configValue; }
    public void setConfigValue(String configValue) { this.configValue = configValue; }

    public String getPreviousValue() { return previousValue; }
    public void setPreviousValue(String previousValue) { this.previousValue = previousValue; }

    public SystemConfigDataType getDataType() { return dataType; }
    public void setDataType(SystemConfigDataType dataType) { this.dataType = dataType; }

    public SystemConfigScope getScope() { return scope; }
    public void setScope(SystemConfigScope scope) { this.scope = scope; }

    public String getServiceName() { return serviceName; }
    public void setServiceName(String serviceName) { this.serviceName = serviceName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public boolean isSensitive() { return isSensitive; }
    public void setSensitive(boolean sensitive) { isSensitive = sensitive; }

    public boolean isHotReload() { return isHotReload; }
    public void setHotReload(boolean hotReload) { isHotReload = hotReload; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public String getAllowedValues() { return allowedValues; }
    public void setAllowedValues(String allowedValues) { this.allowedValues = allowedValues; }

    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }

    public String getUpdateReason() { return updateReason; }
    public void setUpdateReason(String updateReason) { this.updateReason = updateReason; }

    public LocalDateTime getCreatedOn() { return createdOn; }
    public void setCreatedOn(LocalDateTime createdOn) { this.createdOn = createdOn; }

    public LocalDateTime getUpdatedOn() { return updatedOn; }
    public void setUpdatedOn(LocalDateTime updatedOn) { this.updatedOn = updatedOn; }
}