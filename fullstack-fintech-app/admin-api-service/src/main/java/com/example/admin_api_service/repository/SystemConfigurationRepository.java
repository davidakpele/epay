package com.example.admin_api_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.example.admin_api_service.models.systemAndConfiguration.SystemConfiguration;

@Repository
public interface SystemConfigurationRepository extends JpaRepository<SystemConfiguration, String> {

}
