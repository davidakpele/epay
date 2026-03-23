package com.example.admin_api_service.Interfaces;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.example.admin_api_service.enums.SystemConfigScope;
import com.example.admin_api_service.models.systemAndConfiguration.SystemConfiguration;

public interface ISystemConfigurationService {
    SystemConfiguration createConfig(SystemConfiguration config, String createdBy);
 
    SystemConfiguration updateConfig(String configKey, String newValue,
                                     String updatedBy, String updateReason);
 
    SystemConfiguration getConfigById(String configId);
 
    SystemConfiguration getConfigByKey(String configKey);
 
    Optional<SystemConfiguration> findConfigByKey(String configKey);
 
    Page<SystemConfiguration> getAllConfigs(Pageable pageable);
 
    List<SystemConfiguration> getConfigsByService(String serviceName);
 
    List<SystemConfiguration> getConfigsByScope(SystemConfigScope scope);
 
    // Typed value accessors
    String getStringValue(String configKey, String defaultValue);
 
    int getIntValue(String configKey, int defaultValue);
 
    boolean getBooleanValue(String configKey, boolean defaultValue);
 
    double getDoubleValue(String configKey, double defaultValue);
 
    // Roll back config value to previousValue
    SystemConfiguration rollback(String configKey, String rolledBackBy, String reason);
 
    void activateConfig(String configId);
 
    void deactivateConfig(String configId);
 
    void deleteConfig(String configId);
}
