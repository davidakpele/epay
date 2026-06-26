package com.example.admin_api_service.Interfaces;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import com.example.admin_api_service.enums.FeatureFlagRolloutStrategy;
import com.example.admin_api_service.models.systemAndConfiguration.FeatureFlag;

public interface IFeatureFlagService {
    FeatureFlag createFlag(FeatureFlag flag, String createdBy);
 
    FeatureFlag updateFlag(String flagKey, FeatureFlag updated, String updatedBy);
 
    FeatureFlag getFlagById(String flagId);
 
    FeatureFlag getFlagByKey(String flagKey);
 
    Page<FeatureFlag> getAllFlags(Pageable pageable);
 
    List<FeatureFlag> getFlagsByService(String serviceName);
 
    // Core evaluation — returns true if the flag is active for the given context
    boolean isEnabled(String flagKey);
 
    boolean isEnabledForUser(String flagKey, Long userId, String kycTier, String channel);
 
    void enableFlag(String flagKey, String updatedBy, String reason);
 
    void disableFlag(String flagKey, String updatedBy, String reason);
 
    void updateRollout(String flagKey, FeatureFlagRolloutStrategy strategy,
                       Integer rolloutPercentage, String updatedBy);
 
    void addUserToWhitelist(String flagKey, Long userId, String updatedBy);
 
    void removeUserFromWhitelist(String flagKey, Long userId, String updatedBy);
 
    void deleteFlag(String flagId);
}
