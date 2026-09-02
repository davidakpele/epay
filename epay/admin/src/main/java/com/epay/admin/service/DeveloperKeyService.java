package com.epay.admin.service;

import com.epay.common.exception.*;
import com.epay.domain.developer.dto.ApiKeyDTO;
import com.epay.domain.developer.dto.DeveloperAppDTO;
import com.epay.domain.developer.entity.ApiKey;
import com.epay.domain.developer.entity.DeveloperApp;
import com.epay.domain.developer.enums.ApiKeyStatus;
import com.epay.domain.developer.enums.ApiMode;
import com.epay.domain.developer.input.CreateAppRequest;
import com.epay.domain.developer.repository.ApiKeyRepository;
import com.epay.domain.developer.repository.DeveloperAppRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeveloperKeyService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final DeveloperAppRepository appRepository;
    private final ApiKeyRepository       apiKeyRepository;
    private final PasswordEncoder        passwordEncoder;


    @Transactional
    public DeveloperAppDTO createApp(CreateAppRequest request, Long ownerUserId) {
        if (appRepository.existsByOwnerUserIdAndAppName(ownerUserId, request.getAppName()))
            throw new ConflictException("An app with this name already exists",
                    ErrorCode.RESOURCE_ALREADY_EXISTS);

        DeveloperApp app = DeveloperApp.builder()
                .ownerUserId(ownerUserId)
                .appName(request.getAppName())
                .description(request.getDescription())
                .websiteUrl(request.getWebsiteUrl())
                .mode(ApiMode.TEST)
                .liveApproved(false)
                .active(true)
                .build();
        appRepository.save(app);

        ApiKeyDTO testKeys = generateAndSaveKeyPair(app, ApiMode.TEST, ApiKeyStatus.ACTIVE);
        ApiKeyDTO liveKeys = generateAndSaveKeyPair(app, ApiMode.LIVE, ApiKeyStatus.SUSPENDED);

        log.info("[Dev] App created: id={} owner={}", app.getId(), ownerUserId);

        DeveloperAppDTO dto = toAppDTO(app);
        dto.setApiKeys(List.of(testKeys, liveKeys));
        return dto;
    }

    public List<DeveloperAppDTO> getMyApps(Long ownerUserId) {
        return appRepository.findByOwnerUserId(ownerUserId)
                .stream().map(this::toAppDTOWithKeys).toList();
    }

    public DeveloperAppDTO getApp(Long appId, Long callerUserId, boolean isAdmin) {
        DeveloperApp app = requireApp(appId);
        if (!isAdmin && !app.getOwnerUserId().equals(callerUserId))
            throw new ForbiddenException("You do not own this app", ErrorCode.FORBIDDEN_ACCESS);
        return toAppDTOWithKeys(app);
    }

    public Page<DeveloperAppDTO> listAllApps(Pageable pageable) {
        return appRepository.findAllByOrderByCreatedAtDesc(pageable).map(this::toAppDTO);
    }

    @Transactional
    public DeveloperAppDTO updateApp(Long appId, CreateAppRequest request, Long callerUserId) {
        DeveloperApp app = requireOwnedApp(appId, callerUserId);
        if (request.getAppName()    != null) app.setAppName(request.getAppName());
        if (request.getDescription()!= null) app.setDescription(request.getDescription());
        if (request.getWebsiteUrl() != null) app.setWebsiteUrl(request.getWebsiteUrl());
        appRepository.save(app);
        return toAppDTOWithKeys(app);
    }

    @Transactional
    public void deleteApp(Long appId, Long callerUserId) {
        DeveloperApp app = requireOwnedApp(appId, callerUserId);
        appRepository.delete(app);
        log.info("[Dev] App deleted: id={}", appId);
    }


    @Transactional
    public ApiKeyDTO rotateKey(Long appId, ApiMode mode, Long callerUserId) {
        DeveloperApp app = requireOwnedApp(appId, callerUserId);

        if (mode == ApiMode.LIVE && !app.isLiveApproved())
            throw new BadRequestException("Live keys are not enabled for this app. Request live access first.",
                    ErrorCode.INVALID_INPUT);
        apiKeyRepository.findByAppIdAndMode(appId, mode).ifPresent(existing -> {
            apiKeyRepository.revokeKey(existing.getId(), ApiKeyStatus.REVOKED,
                    LocalDateTime.now(), callerUserId, "Rotated by developer");
        });

        ApiKeyStatus newStatus = (mode == ApiMode.LIVE) ? ApiKeyStatus.ACTIVE : ApiKeyStatus.ACTIVE;
        ApiKeyDTO newKey = generateAndSaveKeyPair(app, mode, newStatus);
        log.info("[Dev] Key rotated: appId={} mode={}", appId, mode);
        return newKey;
    }

    @Transactional
    public void revokeKey(Long keyId, Long callerUserId, boolean isAdmin, String reason) {
        ApiKey key = apiKeyRepository.findById(keyId)
                .orElseThrow(() -> new ResourceNotFoundException("API key not found"));

        if (!isAdmin && !key.getApp().getOwnerUserId().equals(callerUserId))
            throw new ForbiddenException("You do not own this key", ErrorCode.FORBIDDEN_ACCESS);

        apiKeyRepository.revokeKey(keyId, ApiKeyStatus.REVOKED,
                LocalDateTime.now(), callerUserId, reason);
        log.info("[Dev] Key revoked: keyId={} by={}", keyId, callerUserId);
    }


    @Transactional
    public DeveloperAppDTO approveLiveMode(Long appId, Long adminUserId) {
        DeveloperApp app = requireApp(appId);
        if (app.isLiveApproved())
            throw new BadRequestException("App already has live access", ErrorCode.INVALID_INPUT);

        app.setLiveApproved(true);
        app.setMode(ApiMode.LIVE);
        app.setLiveApprovedBy(adminUserId);
        app.setLiveApprovedAt(LocalDateTime.now());
        appRepository.save(app);

        apiKeyRepository.findByAppIdAndMode(appId, ApiMode.LIVE).ifPresent(key -> {
            key.setStatus(ApiKeyStatus.ACTIVE);
            apiKeyRepository.save(key);
        });

        log.info("[Dev] Live mode approved: appId={} by={}", appId, adminUserId);
        return toAppDTOWithKeys(app);
    }

    @Transactional
    public DeveloperApp validateApiKey(String publicKey) {
        ApiKey key = apiKeyRepository.findByPublicKey(publicKey)
                .orElseThrow(() -> new ForbiddenException("Invalid API key", ErrorCode.UNAUTHORIZED_ACCESS));

        if (key.getStatus() != ApiKeyStatus.ACTIVE)
            throw new ForbiddenException("API key is " + key.getStatus().name().toLowerCase(),
                    ErrorCode.FORBIDDEN_ACCESS);

        if (key.getExpiresAt() != null && key.getExpiresAt().isBefore(LocalDateTime.now())) {
            apiKeyRepository.revokeKey(key.getId(), ApiKeyStatus.EXPIRED,
                    LocalDateTime.now(), null, "Key expired");
            throw new ForbiddenException("API key has expired", ErrorCode.FORBIDDEN_ACCESS);
        }

        apiKeyRepository.recordUsage(key.getId(), LocalDateTime.now());
        return key.getApp();
    }


    private ApiKeyDTO generateAndSaveKeyPair(DeveloperApp app, ApiMode mode, ApiKeyStatus status) {
        String modeTag    = mode == ApiMode.TEST ? "test" : "live";
        String rawPublic  = "epk_" + modeTag + "_" + randomHex(16);  
        String rawSecret  = "eps_" + modeTag + "_" + randomHex(32);  
        int attempts = 0;
        while (apiKeyRepository.existsByPublicKey(rawPublic) && ++attempts < 5) {
            rawPublic = "epk_" + modeTag + "_" + randomHex(16);
        }

        ApiKey key = ApiKey.builder()
                .app(app)
                .mode(mode)
                .publicKey(rawPublic)
                .secretKeyHash(passwordEncoder.encode(rawSecret))
                .secretKeyPrefix(rawSecret.substring(0, Math.min(rawSecret.length(), 16)))
                .status(status)
                .build();
        apiKeyRepository.save(key);

        return ApiKeyDTO.builder()
                .id(key.getId())
                .mode(mode)
                .publicKey(rawPublic)
                .secretKey(rawSecret)  
                .secretKeyPrefix(key.getSecretKeyPrefix())
                .status(status)
                .requestCount(0)
                .createdAt(key.getCreatedAt())
                .build();
    }

    private String randomHex(int bytes) {
        byte[] buf = new byte[bytes];
        SECURE_RANDOM.nextBytes(buf);
        return HexFormat.of().formatHex(buf);
    }

    private DeveloperApp requireApp(Long appId) {
        return appRepository.findById(appId)
                .orElseThrow(() -> new ResourceNotFoundException("App not found"));
    }

    private DeveloperApp requireOwnedApp(Long appId, Long userId) {
        DeveloperApp app = requireApp(appId);
        if (!app.getOwnerUserId().equals(userId))
            throw new ForbiddenException("You do not own this app", ErrorCode.FORBIDDEN_ACCESS);
        return app;
    }

    private DeveloperAppDTO toAppDTO(DeveloperApp app) {
        return DeveloperAppDTO.builder()
                .id(app.getId())
                .ownerUserId(app.getOwnerUserId())
                .appName(app.getAppName())
                .description(app.getDescription())
                .websiteUrl(app.getWebsiteUrl())
                .mode(app.getMode())
                .liveApproved(app.isLiveApproved())
                .active(app.isActive())
                .createdAt(app.getCreatedAt())
                .updatedAt(app.getUpdatedAt())
                .build();
    }

    private DeveloperAppDTO toAppDTOWithKeys(DeveloperApp app) {
        List<ApiKeyDTO> keys = app.getApiKeys().stream().map(k -> ApiKeyDTO.builder()
                .id(k.getId())
                .mode(k.getMode())
                .publicKey(k.getPublicKey())
                .secretKeyPrefix(k.getSecretKeyPrefix())
                .status(k.getStatus())
                .requestCount(k.getRequestCount())
                .lastUsedAt(k.getLastUsedAt())
                .createdAt(k.getCreatedAt())
                .expiresAt(k.getExpiresAt())
                .build()).toList();

        DeveloperAppDTO dto = toAppDTO(app);
        dto.setApiKeys(keys);
        return dto;
    }
}
