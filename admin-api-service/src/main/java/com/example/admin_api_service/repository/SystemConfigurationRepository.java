package com.example.admin_api_service.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.example.admin_api_service.enums.SystemConfigScope;
import com.example.admin_api_service.models.systemAndConfiguration.SystemConfiguration;

@Repository
public interface SystemConfigurationRepository extends JpaRepository<SystemConfiguration, String> {
    Optional<SystemConfiguration> findByConfigKey(String configKey);
    Optional<SystemConfiguration> findByConfigKeyAndIsActiveTrue(String configKey);
    boolean existsByConfigKey(String configKey);
    List<SystemConfiguration> findAllByServiceNameAndIsActiveTrue(String serviceName);
    List<SystemConfiguration> findAllByScopeAndIsActiveTrue(SystemConfigScope scope);
}
