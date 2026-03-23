package com.example.admin_api_service.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.example.admin_api_service.models.systemAndConfiguration.FeatureFlag;

@Repository
public interface FeatureFlagRepository extends JpaRepository<FeatureFlag, String> {
    Optional<FeatureFlag> findByFlagKey(String flagKey);
    boolean existsByFlagKey(String flagKey);
    List<FeatureFlag> findAllByServiceName(String serviceName);
}