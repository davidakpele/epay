package com.epay.admin.service;

import com.epay.common.exception.*;
import com.epay.domain.developer.dto.WebhookDTO;
import com.epay.domain.developer.entity.AppWebhook;
import com.epay.domain.developer.entity.DeveloperApp;
import com.epay.domain.developer.entity.WebhookDeliveryLog;
import com.epay.domain.developer.enums.ApiMode;
import com.epay.domain.developer.enums.WebhookDeliveryStatus;
import com.epay.domain.developer.enums.WebhookEventType;
import com.epay.domain.developer.input.RegisterWebhookRequest;
import com.epay.domain.developer.repository.AppWebhookRepository;
import com.epay.domain.developer.repository.DeveloperAppRepository;
import com.epay.domain.developer.repository.WebhookDeliveryLogRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class AppWebhookService {
    private static final int MAX_ATTEMPTS = 5;

    private static final int[] RETRY_DELAYS_MINUTES = {1, 5, 30, 120, 480};

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final AppWebhookRepository        webhookRepository;
    private final DeveloperAppRepository      appRepository;
    private final WebhookDeliveryLogRepository deliveryLogRepository;
    private final RestTemplate                restTemplate;
    private final ObjectMapper                objectMapper;

    @Transactional
    public WebhookDTO registerWebhook(Long appId, RegisterWebhookRequest request,
                                       Long callerUserId, boolean isAdmin) {
        DeveloperApp app = requireOwnedApp(appId, callerUserId, isAdmin);

        if (request.getMode() == ApiMode.LIVE && !app.isLiveApproved())
            throw new BadRequestException(
                    "Live webhooks require live access approval first.", ErrorCode.INVALID_INPUT);
        webhookRepository.findByAppIdAndMode(appId, request.getMode())
                .ifPresent(existing -> {
                    existing.setActive(false);
                    webhookRepository.save(existing);
                });

        String signingSecret = generateSigningSecret();

        AppWebhook webhook = AppWebhook.builder()
                .app(app)
                .mode(request.getMode())
                .url(request.getUrl())
                .signingSecret(signingSecret)
                .subscribedEvents(normalizeEvents(request.getSubscribedEvents()))
                .active(true)
                .build();
        webhookRepository.save(webhook);

        log.info("[WebhookSvc] Registered: appId={} mode={} url={}", appId, request.getMode(), request.getUrl());

        WebhookDTO dto = toDTO(webhook);
        dto.setSigningSecret(signingSecret);
        return dto;
    }

    public WebhookDTO getWebhook(Long appId, ApiMode mode, Long callerUserId, boolean isAdmin) {
        requireOwnedApp(appId, callerUserId, isAdmin);
        return webhookRepository.findByAppIdAndMode(appId, mode)
                .map(w -> toDTO(w))
                .orElseThrow(() -> new ResourceNotFoundException("No webhook registered for mode " + mode));
    }

    @Transactional
    public void deleteWebhook(Long appId, ApiMode mode, Long callerUserId, boolean isAdmin) {
        requireOwnedApp(appId, callerUserId, isAdmin);
        webhookRepository.findByAppIdAndMode(appId, mode).ifPresent(w -> {
            w.setActive(false);
            webhookRepository.save(w);
        });
        log.info("[WebhookSvc] Deactivated webhook: appId={} mode={}", appId, mode);
    }


    @Async("liquidityTaskExecutor")
    public void publishEvent(WebhookEventType eventType, Object payload) {
        String eventTypeName = eventType.name();
        List<AppWebhook> subscribers =
                webhookRepository.findActiveSubscribersForEvent(eventTypeName);

        if (subscribers.isEmpty()) return;

        String payloadJson;
        try {
            Map<String, Object> envelope = buildEnvelope(eventType, payload);
            payloadJson = objectMapper.writeValueAsString(envelope);
        } catch (Exception e) {
            log.error("[WebhookSvc] Failed to serialize event {}: {}", eventType, e.getMessage());
            return;
        }

        for (AppWebhook webhook : subscribers) {
            String eventId = "evt_" + UUID.randomUUID().toString().replace("-", "");
            deliverWebhook(webhook, eventId, eventType, payloadJson, 1);
        }
    }

    public Map<String, Object> sendTestEvent(Long appId, ApiMode mode,
                                              Long callerUserId, boolean isAdmin) {
        requireOwnedApp(appId, callerUserId, isAdmin);
        AppWebhook webhook = webhookRepository.findByAppIdAndMode(appId, mode)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No webhook registered for mode " + mode));

        Map<String, Object> testPayload = Map.of(
                "event", "TEST_EVENT",
                "message", "This is a test event from ePay",
                "appId", appId,
                "mode", mode.name(),
                "timestamp", System.currentTimeMillis());

        String payloadJson;
        try {
            payloadJson = objectMapper.writeValueAsString(testPayload);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize test payload", e);
        }

        String signature = signPayload(payloadJson, webhook.getSigningSecret());
        long start = System.currentTimeMillis();
        try {
            HttpHeaders headers = buildHeaders(signature, "TEST_EVENT",
                    "evt_test_" + System.currentTimeMillis());
            ResponseEntity<String> response = restTemplate.postForEntity(
                    webhook.getUrl(),
                    new HttpEntity<>(payloadJson, headers),
                    String.class);
            long duration = System.currentTimeMillis() - start;
            return Map.of(
                    "delivered", true,
                    "httpStatus", response.getStatusCode().value(),
                    "durationMs", duration,
                    "url", webhook.getUrl());
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - start;
            return Map.of(
                    "delivered", false,
                    "error", e.getMessage(),
                    "durationMs", duration,
                    "url", webhook.getUrl());
        }
    }

    public Page<WebhookDeliveryLog> getDeliveryLogs(Long webhookId, Pageable pageable) {
        return deliveryLogRepository.findByWebhookIdOrderByCreatedAtDesc(webhookId, pageable);
    }

    public Page<WebhookDeliveryLog> getAppDeliveryLogs(Long appId, Pageable pageable) {
        return deliveryLogRepository.findByAppIdOrderByCreatedAtDesc(appId, pageable);
    }

    @Scheduled(fixedDelayString = "${epay.webhooks.retry-interval-ms:60000}")
    @Transactional
    public void retryFailedDeliveries() {
        List<WebhookDeliveryLog> due = deliveryLogRepository.findDueRetries(LocalDateTime.now());
        if (due.isEmpty()) return;
        log.debug("[WebhookSvc] Retrying {} failed deliveries", due.size());

        for (WebhookDeliveryLog log_ : due) {
            webhookRepository.findById(log_.getWebhookId()).ifPresent(webhook -> {
                if (!webhook.isActive()) {
                    markAbandoned(log_);
                    return;
                }
                int nextAttempt = log_.getAttemptNumber() + 1;
                if (nextAttempt > MAX_ATTEMPTS) {
                    markAbandoned(log_);
                } else {
                    deliverWebhook(webhook, log_.getEventId(),
                            log_.getEventType(), log_.getPayload(), nextAttempt);
                }
            });
        }
    }

    private void deliverWebhook(AppWebhook webhook, String eventId,
                                 WebhookEventType eventType, String payloadJson,
                                 int attemptNumber) {
        String signature = signPayload(payloadJson, webhook.getSigningSecret());
        long start = System.currentTimeMillis();
        WebhookDeliveryLog logEntry = WebhookDeliveryLog.builder()
                .webhookId(webhook.getId())
                .appId(webhook.getApp().getId())
                .eventId(eventId)
                .eventType(eventType)
                .targetUrl(webhook.getUrl())
                .payload(payloadJson)
                .status(WebhookDeliveryStatus.PENDING)
                .attemptNumber(attemptNumber)
                .build();
        deliveryLogRepository.save(logEntry);

        try {
            HttpHeaders headers = buildHeaders(signature, eventType.name(), eventId);
            ResponseEntity<String> response = restTemplate.postForEntity(
                    webhook.getUrl(),
                    new HttpEntity<>(payloadJson, headers),
                    String.class);

            long duration = System.currentTimeMillis() - start;
            boolean success = response.getStatusCode().is2xxSuccessful();

            logEntry.setHttpStatusCode(response.getStatusCode().value());
            logEntry.setResponseBody(truncate(response.getBody(), 500));
            logEntry.setDurationMs(duration);

            if (success) {
                logEntry.setStatus(WebhookDeliveryStatus.DELIVERED);
                webhookRepository.incrementSuccess(webhook.getId(), LocalDateTime.now());
                log.debug("[WebhookSvc] Delivered: eventId={} url={} status={}",
                        eventId, webhook.getUrl(), response.getStatusCode().value());
            } else {
                scheduleRetry(logEntry, attemptNumber, webhook);
            }

        } catch (Exception e) {
            long duration = System.currentTimeMillis() - start;
            logEntry.setErrorMessage(truncate(e.getMessage(), 500));
            logEntry.setDurationMs(duration);
            scheduleRetry(logEntry, attemptNumber, webhook);
            log.warn("[WebhookSvc] Delivery failed attempt={} eventId={}: {}",
                    attemptNumber, eventId, e.getMessage());
        }

        deliveryLogRepository.save(logEntry);
    }

    private void scheduleRetry(WebhookDeliveryLog logEntry, int attemptNumber, AppWebhook webhook) {
        if (attemptNumber >= MAX_ATTEMPTS) {
            logEntry.setStatus(WebhookDeliveryStatus.ABANDONED);
            webhookRepository.incrementFailure(webhook.getId(), LocalDateTime.now());
            log.warn("[WebhookSvc] ABANDONED after {} attempts: eventId={}", attemptNumber, logEntry.getEventId());
        } else {
            int delayMinutes = RETRY_DELAYS_MINUTES[Math.min(attemptNumber - 1, RETRY_DELAYS_MINUTES.length - 1)];
            logEntry.setStatus(WebhookDeliveryStatus.RETRYING);
            logEntry.setNextRetryAt(LocalDateTime.now().plusMinutes(delayMinutes));
            webhookRepository.incrementFailure(webhook.getId(), LocalDateTime.now());
        }
    }

    private void markAbandoned(WebhookDeliveryLog entry) {
        entry.setStatus(WebhookDeliveryStatus.ABANDONED);
        deliveryLogRepository.save(entry);
    }

    private String signPayload(String payload, String signingSecret) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(
                    signingSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return "sha256=" + java.util.HexFormat.of().formatHex(
                    mac.doFinal(payload.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            log.error("[WebhookSvc] Signing failed: {}", e.getMessage());
            return "";
        }
    }

    private HttpHeaders buildHeaders(String signature, String eventType, String eventId) {
        HttpHeaders h = new HttpHeaders();
        h.setContentType(MediaType.APPLICATION_JSON);
        h.set("X-Epay-Signature",   signature);
        h.set("X-Epay-Event",       eventType);
        h.set("X-Epay-Delivery-Id", eventId);
        h.set("User-Agent",         "ePay-Webhook/1.0");
        return h;
    }

    private Map<String, Object> buildEnvelope(WebhookEventType eventType, Object payload) {
        Map<String, Object> envelope = new LinkedHashMap<>();
        envelope.put("event",     eventType.name());
        envelope.put("eventId",   "evt_" + UUID.randomUUID().toString().replace("-", ""));
        envelope.put("timestamp", System.currentTimeMillis());
        envelope.put("data",      payload);
        return envelope;
    }

    private String generateSigningSecret() {
        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);
        return "whsec_" + java.util.HexFormat.of().formatHex(bytes);
    }

    private String normalizeEvents(String raw) {
        if (raw == null || raw.isBlank()) return null;
        return Arrays.stream(raw.split("[,\\s]+"))
                .map(String::trim)
                .map(String::toUpperCase)
                .filter(s -> !s.isEmpty())
                .reduce((a, b) -> a + "," + b)
                .orElse(null);
    }

    private String truncate(String s, int max) {
        if (s == null) return null;
        return s.length() > max ? s.substring(0, max) + "…" : s;
    }

    private DeveloperApp requireOwnedApp(Long appId, Long userId, boolean isAdmin) {
        DeveloperApp app = appRepository.findById(appId)
                .orElseThrow(() -> new ResourceNotFoundException("App not found"));
        if (!isAdmin && !app.getOwnerUserId().equals(userId))
            throw new ForbiddenException("You do not own this app", ErrorCode.FORBIDDEN_ACCESS);
        return app;
    }

    private WebhookDTO toDTO(AppWebhook w) {
        return WebhookDTO.builder()
                .id(w.getId())
                .appId(w.getApp().getId())
                .mode(w.getMode())
                .url(w.getUrl())
                .subscribedEvents(w.getSubscribedEvents())
                .active(w.isActive())
                .successCount(w.getSuccessCount())
                .failureCount(w.getFailureCount())
                .lastDeliveryAt(w.getLastDeliveryAt())
                .lastDeliveryStatus(w.getLastDeliveryStatus())
                .createdAt(w.getCreatedAt())
                .build();
    }
}
