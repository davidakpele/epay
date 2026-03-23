package com.example.admin_api_service.services;

import com.example.admin_api_service.Interfaces.IWebhookEndpointService;
import com.example.admin_api_service.enums.WebhookEndpointStatus;
import com.example.admin_api_service.exceptions.ConflictException;
import com.example.admin_api_service.exceptions.ResourceNotFoundException;
import com.example.admin_api_service.models.notificationsAndComms.WebhookEndpoint;
import com.example.admin_api_service.repository.WebhookEndpointRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;

@Service
@Transactional
public class WebhookEndpointServiceImpl implements IWebhookEndpointService {

    private final WebhookEndpointRepository endpointRepository;
    private final PasswordEncoder passwordEncoder;

    public WebhookEndpointServiceImpl(WebhookEndpointRepository endpointRepository,
                                       PasswordEncoder passwordEncoder) {
        this.endpointRepository = endpointRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public WebhookEndpoint registerEndpoint(WebhookEndpoint endpoint, String createdBy) {
        // Generate and hash the HMAC secret — return plain text to caller once
        String plainSecret = generateSecret();
        endpoint.setSecretHash(passwordEncoder.encode(plainSecret));
        // Store plain secret temporarily in a non-persistent field for the response
        // In production, return it via the response DTO — not persisted
        endpoint.setStatus(WebhookEndpointStatus.ACTIVE);
        endpoint.setCreatedBy(createdBy);
        endpoint.setTotalDeliveries(0);
        endpoint.setSuccessfulDeliveries(0);
        endpoint.setFailedDeliveries(0);
        endpoint.setConsecutiveFailures(0);

        WebhookEndpoint saved = endpointRepository.save(endpoint);
        // Attach plain secret to the transient field so the controller can return it
        saved.setSecretHash(plainSecret); // Replace hash with plain for one-time display
        return saved;
    }

    @Override
    public WebhookEndpoint updateEndpoint(String endpointId, WebhookEndpoint updated,
                                           String updatedBy) {
        WebhookEndpoint existing = getEndpointById(endpointId);
        existing.setName(updated.getName());
        existing.setDescription(updated.getDescription());
        existing.setUrl(updated.getUrl());
        existing.setSubscribedEvents(updated.getSubscribedEvents());
        existing.setCustomHeaders(updated.getCustomHeaders());
        existing.setHttpMethod(updated.getHttpMethod());
        existing.setTimeoutSeconds(updated.getTimeoutSeconds());
        existing.setMaxRetries(updated.getMaxRetries());
        existing.setSslVerify(updated.isSslVerify());
        existing.setEnvironment(updated.getEnvironment());
        existing.setDisableAfterFailures(updated.getDisableAfterFailures());
        existing.setUpdatedBy(updatedBy);
        existing.setUpdatedOn(LocalDateTime.now());
        return endpointRepository.save(existing);
    }

    @Override
    @Transactional(readOnly = true)
    public WebhookEndpoint getEndpointById(String endpointId) {
        return endpointRepository.findById(endpointId)
                .orElseThrow(() -> new ResourceNotFoundException("WebhookEndpoint", "id", endpointId));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<WebhookEndpoint> getAllEndpoints(Pageable pageable) {
        return endpointRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<WebhookEndpoint> getEndpointsByStatus(WebhookEndpointStatus status, Pageable pageable) {
        return endpointRepository.findAllByStatus(status, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<WebhookEndpoint> getEndpointsByOwner(String ownerId, String ownerType) {
        return endpointRepository.findAllByOwnerIdAndOwnerType(ownerId, ownerType);
    }

    @Override
    @Transactional(readOnly = true)
    public List<WebhookEndpoint> getSubscribersForEvent(String eventType) {
        return endpointRepository.findActiveSubscribersForEvent(eventType);
    }

    @Override
    public void activateEndpoint(String endpointId, String updatedBy) {
        WebhookEndpoint endpoint = getEndpointById(endpointId);
        endpoint.setStatus(WebhookEndpointStatus.ACTIVE);
        endpoint.setConsecutiveFailures(0);
        endpoint.setDisabledReason(null);
        endpoint.setUpdatedBy(updatedBy);
        endpoint.setUpdatedOn(LocalDateTime.now());
        endpointRepository.save(endpoint);
    }

    @Override
    public void suspendEndpoint(String endpointId, String updatedBy) {
        WebhookEndpoint endpoint = getEndpointById(endpointId);
        endpoint.setStatus(WebhookEndpointStatus.SUSPENDED);
        endpoint.setUpdatedBy(updatedBy);
        endpoint.setUpdatedOn(LocalDateTime.now());
        endpointRepository.save(endpoint);
    }

    @Override
    public void recordSuccess(String endpointId) {
        endpointRepository.findById(endpointId).ifPresent(endpoint -> {
            endpoint.setTotalDeliveries(endpoint.getTotalDeliveries() + 1);
            endpoint.setSuccessfulDeliveries(endpoint.getSuccessfulDeliveries() + 1);
            endpoint.setConsecutiveFailures(0);
            endpoint.setLastSuccessAt(LocalDateTime.now());
            endpoint.setUpdatedOn(LocalDateTime.now());

            // Re-activate if it was auto-disabled and now succeeding
            if (endpoint.getStatus() == WebhookEndpointStatus.AUTO_DISABLED) {
                endpoint.setStatus(WebhookEndpointStatus.ACTIVE);
            }
            endpointRepository.save(endpoint);
        });
    }

    @Override
    public void recordFailure(String endpointId) {
        endpointRepository.findById(endpointId).ifPresent(endpoint -> {
            endpoint.setTotalDeliveries(endpoint.getTotalDeliveries() + 1);
            endpoint.setFailedDeliveries(endpoint.getFailedDeliveries() + 1);
            int consecutive = endpoint.getConsecutiveFailures() + 1;
            endpoint.setConsecutiveFailures(consecutive);
            endpoint.setLastFailureAt(LocalDateTime.now());
            endpoint.setUpdatedOn(LocalDateTime.now());

            if (consecutive >= endpoint.getDisableAfterFailures()) {
                endpoint.setStatus(WebhookEndpointStatus.AUTO_DISABLED);
                endpoint.setDisabledReason("Auto-disabled after " + consecutive
                        + " consecutive failures");
            }
            endpointRepository.save(endpoint);
        });
    }

    @Override
    public String rotateSecret(String endpointId, String updatedBy) {
        WebhookEndpoint endpoint = getEndpointById(endpointId);
        String plainSecret = generateSecret();
        endpoint.setSecretHash(passwordEncoder.encode(plainSecret));
        endpoint.setUpdatedBy(updatedBy);
        endpoint.setUpdatedOn(LocalDateTime.now());
        endpointRepository.save(endpoint);
        return plainSecret; // Return plain secret once — never again
    }

    @Override
    public void deleteEndpoint(String endpointId) {
        WebhookEndpoint endpoint = getEndpointById(endpointId);
        if (endpoint.getStatus() == WebhookEndpointStatus.ACTIVE) {
            throw new ConflictException("Active endpoints cannot be deleted. Suspend first.");
        }
        endpointRepository.delete(endpoint);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean validateSignature(String endpointId, String payload,
                                      String incomingSignature) {
        // Note: bcrypt is one-way — this validates using HMAC-SHA256 instead.
        // The secretHash should be stored as the raw secret encrypted with a reversible
        // cipher (e.g. AES) in production. This is a simplified validation stub.
        WebhookEndpoint endpoint = getEndpointById(endpointId);
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            // In production, decrypt the stored secret before using it
            mac.init(new SecretKeySpec(
                    endpoint.getSecretHash().getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] expected = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            String expectedHex = Base64.getEncoder().encodeToString(expected);
            return expectedHex.equals(incomingSignature);
        } catch (Exception e) {
            return false;
        }
    }

    // ── Private helpers ────────────────────────────────────────────────────────

    private String generateSecret() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}