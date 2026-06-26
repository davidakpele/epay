package com.example.admin_api_service.services;

import com.example.admin_api_service.Interfaces.IFeatureFlagService;
import com.example.admin_api_service.enums.FeatureFlagRolloutStrategy;
import com.example.admin_api_service.exceptions.ConflictException;
import com.example.admin_api_service.exceptions.ResourceNotFoundException;
import com.example.admin_api_service.models.systemAndConfiguration.FeatureFlag;
import com.example.admin_api_service.repository.FeatureFlagRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Service
@Transactional
public class FeatureFlagServiceImpl implements IFeatureFlagService {

    private final FeatureFlagRepository featureFlagRepository;
    private final ObjectMapper objectMapper;

    public FeatureFlagServiceImpl(FeatureFlagRepository featureFlagRepository,
                                   ObjectMapper objectMapper) {
        this.featureFlagRepository = featureFlagRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public FeatureFlag createFlag(FeatureFlag flag, String createdBy) {
        if (featureFlagRepository.existsByFlagKey(flag.getFlagKey())) {
            throw new ConflictException("Feature flag with key '" + flag.getFlagKey() + "' already exists");
        }
        flag.setEnabled(false);
        flag.setUpdatedBy(createdBy);
        return featureFlagRepository.save(flag);
    }

    @Override
    public FeatureFlag updateFlag(String flagKey, FeatureFlag updated, String updatedBy) {
        FeatureFlag existing = getFlagByKey(flagKey);

        if (!existing.getFlagKey().equals(updated.getFlagKey())
                && featureFlagRepository.existsByFlagKey(updated.getFlagKey())) {
            throw new ConflictException("Feature flag with key '" + updated.getFlagKey() + "' already exists");
        }

        existing.setName(updated.getName());
        existing.setDescription(updated.getDescription());
        existing.setServiceName(updated.getServiceName());
        existing.setRolloutStrategy(updated.getRolloutStrategy());
        existing.setRolloutPercentage(updated.getRolloutPercentage());
        existing.setApplicableTiers(updated.getApplicableTiers());
        existing.setApplicableChannels(updated.getApplicableChannels());
        existing.setEnvironment(updated.getEnvironment());
        existing.setExpiresAt(updated.getExpiresAt());
        existing.setUpdatedBy(updatedBy);
        existing.setUpdatedOn(LocalDateTime.now());
        return featureFlagRepository.save(existing);
    }

    @Override
    @Transactional(readOnly = true)
    public FeatureFlag getFlagById(String flagId) {
        return featureFlagRepository.findById(flagId)
                .orElseThrow(() -> new ResourceNotFoundException("FeatureFlag", "id", flagId));
    }

    @Override
    @Transactional(readOnly = true)
    public FeatureFlag getFlagByKey(String flagKey) {
        return featureFlagRepository.findByFlagKey(flagKey)
                .orElseThrow(() -> new ResourceNotFoundException("FeatureFlag", "flagKey", flagKey));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FeatureFlag> getAllFlags(Pageable pageable) {
        return featureFlagRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FeatureFlag> getFlagsByService(String serviceName) {
        return featureFlagRepository.findAllByServiceName(serviceName);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isEnabled(String flagKey) {
        return featureFlagRepository.findByFlagKey(flagKey)
                .map(flag -> flag.isEnabled()
                        && (flag.getExpiresAt() == null || flag.getExpiresAt().isAfter(LocalDateTime.now())))
                .orElse(false);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isEnabledForUser(String flagKey, Long userId, String kycTier, String channel) {
        FeatureFlag flag = featureFlagRepository.findByFlagKey(flagKey).orElse(null);
        if (flag == null || !flag.isEnabled()) return false;
        if (flag.getExpiresAt() != null && flag.getExpiresAt().isBefore(LocalDateTime.now())) return false;

        return switch (flag.getRolloutStrategy()) {
            case ALL_OR_NOTHING -> true;
            case PERCENTAGE_ROLLOUT -> evaluatePercentage(flag, userId);
            case USER_WHITELIST -> isUserInWhitelist(flag, userId);
            case KYC_TIER -> isTierApplicable(flag, kycTier);
            case CHANNEL -> isChannelApplicable(flag, channel);
            case INTERNAL_ONLY -> false; // handled at the gateway level
        };
    }

    @Override
    public void enableFlag(String flagKey, String updatedBy, String reason) {
        FeatureFlag flag = getFlagByKey(flagKey);
        flag.setEnabled(true);
        flag.setEnabledAt(LocalDateTime.now());
        flag.setDisabledAt(null);
        flag.setUpdateReason(reason);
        flag.setUpdatedBy(updatedBy);
        flag.setUpdatedOn(LocalDateTime.now());
        featureFlagRepository.save(flag);
    }

    @Override
    public void disableFlag(String flagKey, String updatedBy, String reason) {
        FeatureFlag flag = getFlagByKey(flagKey);
        flag.setEnabled(false);
        flag.setDisabledAt(LocalDateTime.now());
        flag.setUpdateReason(reason);
        flag.setUpdatedBy(updatedBy);
        flag.setUpdatedOn(LocalDateTime.now());
        featureFlagRepository.save(flag);
    }

    @Override
    public void updateRollout(String flagKey, FeatureFlagRolloutStrategy strategy,
                               Integer rolloutPercentage, String updatedBy) {
        FeatureFlag flag = getFlagByKey(flagKey);
        flag.setRolloutStrategy(strategy);
        flag.setRolloutPercentage(rolloutPercentage);
        flag.setUpdatedBy(updatedBy);
        flag.setUpdatedOn(LocalDateTime.now());
        featureFlagRepository.save(flag);
    }

    @Override
    public void addUserToWhitelist(String flagKey, Long userId, String updatedBy) {
        FeatureFlag flag = getFlagByKey(flagKey);
        List<Long> whitelist = parseWhitelist(flag.getWhitelistedUserIds());
        if (!whitelist.contains(userId)) {
            whitelist.add(userId);
            flag.setWhitelistedUserIds(serializeList(whitelist));
            flag.setUpdatedBy(updatedBy);
            flag.setUpdatedOn(LocalDateTime.now());
            featureFlagRepository.save(flag);
        }
    }

    @Override
    public void removeUserFromWhitelist(String flagKey, Long userId, String updatedBy) {
        FeatureFlag flag = getFlagByKey(flagKey);
        List<Long> whitelist = parseWhitelist(flag.getWhitelistedUserIds());
        whitelist.remove(userId);
        flag.setWhitelistedUserIds(serializeList(whitelist));
        flag.setUpdatedBy(updatedBy);
        flag.setUpdatedOn(LocalDateTime.now());
        featureFlagRepository.save(flag);
    }

    @Override
    public void deleteFlag(String flagId) {
        FeatureFlag flag = getFlagById(flagId);
        if (flag.isEnabled()) {
            throw new ConflictException("Enabled feature flags cannot be deleted. Disable first.");
        }
        featureFlagRepository.delete(flag);
    }

    // ── Private helpers ────────────────────────────────────────────────────────

    private boolean evaluatePercentage(FeatureFlag flag, Long userId) {
        if (flag.getRolloutPercentage() == null) return false;
        int bucket = (int) (Math.abs(userId.hashCode()) % 100);
        return bucket < flag.getRolloutPercentage();
    }

    private boolean isUserInWhitelist(FeatureFlag flag, Long userId) {
        return parseWhitelist(flag.getWhitelistedUserIds()).contains(userId);
    }

    private boolean isTierApplicable(FeatureFlag flag, String kycTier) {
        if (flag.getApplicableTiers() == null || kycTier == null) return false;
        return flag.getApplicableTiers().contains(kycTier);
    }

    private boolean isChannelApplicable(FeatureFlag flag, String channel) {
        if (flag.getApplicableChannels() == null || channel == null) return false;
        return flag.getApplicableChannels().contains(channel);
    }

    private List<Long> parseWhitelist(String json) {
        if (json == null || json.isBlank()) return new ArrayList<>();
        try {
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    private String serializeList(List<Long> list) {
        try {
            return objectMapper.writeValueAsString(list);
        } catch (Exception e) {
            return "[]";
        }
    }
}