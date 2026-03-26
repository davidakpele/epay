package com.example.admin_api_service.services;

import com.example.admin_api_service.Interfaces.ISystemConfigurationService;
import com.example.admin_api_service.enums.SystemConfigScope;
import com.example.admin_api_service.exceptions.BadRequestException;
import com.example.admin_api_service.exceptions.ConflictException;
import com.example.admin_api_service.exceptions.ResourceNotFoundException;
import com.example.admin_api_service.models.systemAndConfiguration.SystemConfiguration;
import com.example.admin_api_service.repository.SystemConfigurationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class SystemConfigurationServiceImpl implements ISystemConfigurationService {

    private final SystemConfigurationRepository configRepository;

    public SystemConfigurationServiceImpl(SystemConfigurationRepository configRepository) {
        this.configRepository = configRepository;
    }

    @Override
    public SystemConfiguration createConfig(SystemConfiguration config, String createdBy) {
        if (configRepository.existsByConfigKey(config.getConfigKey())) {
            throw new ConflictException("Config with key '" + config.getConfigKey() + "' already exists");
        }
        config.setActive(true);
        config.setUpdatedBy(createdBy);
        return configRepository.save(config);
    }

    @Override
    public SystemConfiguration updateConfig(String configKey, String newValue,
                                             String updatedBy, String updateReason) {
        SystemConfiguration config = getConfigByKey(configKey);

        if (config.getAllowedValues() != null && !config.getAllowedValues().contains(newValue)) {
            throw new BadRequestException("Value '" + newValue
                    + "' is not in the allowed values list for config: " + configKey);
        }

        config.setPreviousValue(config.getConfigValue());
        config.setConfigValue(newValue);
        config.setUpdatedBy(updatedBy);
        config.setUpdateReason(updateReason);
        config.setUpdatedOn(LocalDateTime.now());
        return configRepository.save(config);
    }

    @Override
    @Transactional(readOnly = true)
    public SystemConfiguration getConfigById(String configId) {
        return configRepository.findById(configId)
                .orElseThrow(() -> new ResourceNotFoundException("SystemConfiguration", "id", configId));
    }

    @Override
    @Transactional(readOnly = true)
    public SystemConfiguration getConfigByKey(String configKey) {
        return configRepository.findByConfigKey(configKey)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "SystemConfiguration", "configKey", configKey));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<SystemConfiguration> findConfigByKey(String configKey) {
        return configRepository.findByConfigKey(configKey);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SystemConfiguration> getAllConfigs(Pageable pageable) {
        return configRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SystemConfiguration> getConfigsByService(String serviceName) {
        return configRepository.findAllByServiceNameAndIsActiveTrue(serviceName);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SystemConfiguration> getConfigsByScope(SystemConfigScope scope) {
        return configRepository.findAllByScopeAndIsActiveTrue(scope);
    }

    @Override
    @Transactional(readOnly = true)
    public String getStringValue(String configKey, String defaultValue) {
        return configRepository.findByConfigKeyAndIsActiveTrue(configKey)
                .map(SystemConfiguration::getConfigValue)
                .orElse(defaultValue);
    }

    @Override
    @Transactional(readOnly = true)
    public int getIntValue(String configKey, int defaultValue) {
        return configRepository.findByConfigKeyAndIsActiveTrue(configKey)
                .map(c -> {
                    try { return Integer.parseInt(c.getConfigValue()); }
                    catch (NumberFormatException e) { return defaultValue; }
                }).orElse(defaultValue);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean getBooleanValue(String configKey, boolean defaultValue) {
        return configRepository.findByConfigKeyAndIsActiveTrue(configKey)
                .map(c -> Boolean.parseBoolean(c.getConfigValue()))
                .orElse(defaultValue);
    }

    @Override
    @Transactional(readOnly = true)
    public double getDoubleValue(String configKey, double defaultValue) {
        return configRepository.findByConfigKeyAndIsActiveTrue(configKey)
                .map(c -> {
                    try { return Double.parseDouble(c.getConfigValue()); }
                    catch (NumberFormatException e) { return defaultValue; }
                }).orElse(defaultValue);
    }

    @Override
    public SystemConfiguration rollback(String configKey, String rolledBackBy, String reason) {
        SystemConfiguration config = getConfigByKey(configKey);
        if (config.getPreviousValue() == null) {
            throw new BadRequestException("No previous value to roll back to for config: " + configKey);
        }
        String current = config.getConfigValue();
        config.setConfigValue(config.getPreviousValue());
        config.setPreviousValue(current);
        config.setUpdatedBy(rolledBackBy);
        config.setUpdateReason("ROLLBACK: " + reason);
        config.setUpdatedOn(LocalDateTime.now());
        return configRepository.save(config);
    }

    @Override
    public void activateConfig(String configId) {
        SystemConfiguration config = getConfigById(configId);
        config.setActive(true);
        config.setUpdatedOn(LocalDateTime.now());
        configRepository.save(config);
    }

    @Override
    public void deactivateConfig(String configId) {
        SystemConfiguration config = getConfigById(configId);
        config.setActive(false);
        config.setUpdatedOn(LocalDateTime.now());
        configRepository.save(config);
    }

    @Override
    public void deleteConfig(String configId) {
        SystemConfiguration config = getConfigById(configId);
        if (config.isActive()) {
            throw new ConflictException("Active configurations cannot be deleted. Deactivate first.");
        }
        configRepository.delete(config);
    }
}
