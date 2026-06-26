package com.example.admin_api_service.services;

import com.example.admin_api_service.Interfaces.IWebhookDeliveryLogService;
import com.example.admin_api_service.Interfaces.IWebhookEndpointService;
import com.example.admin_api_service.enums.WebhookDeliveryStatus;
import com.example.admin_api_service.exceptions.BadRequestException;
import com.example.admin_api_service.exceptions.ResourceNotFoundException;
import com.example.admin_api_service.models.notificationsAndComms.WebhookDeliveryLog;
import com.example.admin_api_service.repository.WebhookDeliveryLogRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class WebhookDeliveryLogServiceImpl implements IWebhookDeliveryLogService {

    // Exponential backoff retry delays in minutes: attempt 1 = 1m, 2 = 5m, 3 = 30m
    private static final int[] RETRY_DELAYS_MINUTES = {1, 5, 30};
    private static final int HTTP_SUCCESS_MIN = 200;
    private static final int HTTP_SUCCESS_MAX = 299;

    private final WebhookDeliveryLogRepository deliveryLogRepository;
    private final IWebhookEndpointService endpointService;

    public WebhookDeliveryLogServiceImpl(WebhookDeliveryLogRepository deliveryLogRepository,
                                          IWebhookEndpointService endpointService) {
        this.deliveryLogRepository = deliveryLogRepository;
        this.endpointService = endpointService;
    }

    @Override
    public WebhookDeliveryLog deliver(String webhookEndpointId, String eventType,
                                      String eventReferenceId, String eventReferenceType,
                                      String requestPayload) {
        endpointService.getEndpointById(webhookEndpointId); 

        String idempotencyKey = webhookEndpointId + ":" + eventType + ":" + eventReferenceId;
        if (deliveryLogRepository.existsByIdempotencyKeyAndStatusIn(
                idempotencyKey,
                List.of(WebhookDeliveryStatus.SUCCESS,
                        WebhookDeliveryStatus.DELIVERING,
                        WebhookDeliveryStatus.PENDING))) {
            return deliveryLogRepository.findByIdempotencyKey(idempotencyKey)
                    .orElseThrow();
        }

        WebhookDeliveryLog log = new WebhookDeliveryLog();
        log.setWebhookEndpointId(webhookEndpointId);
        log.setEventType(eventType);
        log.setEventReferenceId(eventReferenceId);
        log.setEventReferenceType(eventReferenceType);
        log.setRequestPayload(requestPayload);
        log.setIdempotencyKey(idempotencyKey);
        log.setAttemptNumber(1);
        log.setManualReplay(false);
        log.setStatus(WebhookDeliveryStatus.PENDING);
        return deliveryLogRepository.save(log);
    }

    @Override
    @Transactional(readOnly = true)
    public WebhookDeliveryLog getLogById(String logId) {
        return deliveryLogRepository.findById(logId)
                .orElseThrow(() -> new ResourceNotFoundException("WebhookDeliveryLog", "id", logId));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<WebhookDeliveryLog> getAllLogs(Pageable pageable) {
        return deliveryLogRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<WebhookDeliveryLog> getLogsByEndpoint(String endpointId, Pageable pageable) {
        return deliveryLogRepository.findAllByWebhookEndpointId(endpointId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<WebhookDeliveryLog> getLogsByStatus(WebhookDeliveryStatus status, Pageable pageable) {
        return deliveryLogRepository.findAllByStatus(status, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<WebhookDeliveryLog> getLogsByEventType(String eventType, Pageable pageable) {
        return deliveryLogRepository.findAllByEventType(eventType, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<WebhookDeliveryLog> getLogsForEventReference(String eventReferenceId) {
        return deliveryLogRepository.findAllByEventReferenceId(eventReferenceId);
    }

    @Override
    public WebhookDeliveryLog recordResponse(String logId, int httpStatusCode,
                                              String responseBody, String responseHeaders,
                                              long durationMs) {
        WebhookDeliveryLog log = getLogById(logId);
        log.setResponseStatusCode(httpStatusCode);
        log.setResponseBody(truncate(responseBody, 2000));
        log.setResponseHeaders(responseHeaders);
        log.setDurationMs(durationMs);
        log.setUpdatedOn(LocalDateTime.now());

        boolean success = httpStatusCode >= HTTP_SUCCESS_MIN && httpStatusCode <= HTTP_SUCCESS_MAX;
        if (success) {
            log.setStatus(WebhookDeliveryStatus.SUCCESS);
            log.setDeliveredAt(LocalDateTime.now());
            log.setNextRetryAt(null);
            endpointService.recordSuccess(log.getWebhookEndpointId());
        } else {
            scheduleRetryOrFail(log);
            endpointService.recordFailure(log.getWebhookEndpointId());
        }

        return deliveryLogRepository.save(log);
    }

    @Override
    public WebhookDeliveryLog recordError(String logId, String failureReason) {
        WebhookDeliveryLog log = getLogById(logId);
        log.setFailureReason(failureReason);
        log.setUpdatedOn(LocalDateTime.now());
        scheduleRetryOrFail(log);
        endpointService.recordFailure(log.getWebhookEndpointId());
        return deliveryLogRepository.save(log);
    }

    @Override
    public WebhookDeliveryLog replay(String logId, String replayedBy) {
        WebhookDeliveryLog original = getLogById(logId);
        if (original.getStatus() != WebhookDeliveryStatus.FAILED) {
            throw new BadRequestException("Only FAILED deliveries can be replayed");
        }

        endpointService.getEndpointById(original.getWebhookEndpointId());

        WebhookDeliveryLog replay = new WebhookDeliveryLog();
        replay.setWebhookEndpointId(original.getWebhookEndpointId());
        replay.setEventType(original.getEventType());
        replay.setEventReferenceId(original.getEventReferenceId());
        replay.setEventReferenceType(original.getEventReferenceType());
        replay.setRequestPayload(original.getRequestPayload());
        replay.setIdempotencyKey(original.getIdempotencyKey() + ":replay:" + UUID.randomUUID());
        replay.setAttemptNumber(original.getAttemptNumber() + 1);
        replay.setManualReplay(true);
        replay.setReplayedBy(replayedBy);
        replay.setStatus(WebhookDeliveryStatus.PENDING);
        return deliveryLogRepository.save(replay);
    }

    @Override
    @Scheduled(fixedDelay = 30000) // every 30 seconds
    public void processRetries() {
        List<WebhookDeliveryLog> due = deliveryLogRepository
                .findAllByStatusAndNextRetryAtBeforeOrEqual(
                        WebhookDeliveryStatus.RETRYING, LocalDateTime.now());
        due.forEach(log -> {
            log.setStatus(WebhookDeliveryStatus.DELIVERING);
            log.setUpdatedOn(LocalDateTime.now());
        });
        if (!due.isEmpty()) {
            deliveryLogRepository.saveAll(due);
        }
    }

    private void scheduleRetryOrFail(WebhookDeliveryLog log) {
        int retryIndex = log.getAttemptNumber() - 1;
        if (retryIndex < RETRY_DELAYS_MINUTES.length) {
            log.setStatus(WebhookDeliveryStatus.RETRYING);
            log.setNextRetryAt(
                    LocalDateTime.now().plusMinutes(RETRY_DELAYS_MINUTES[retryIndex]));
            log.setAttemptNumber(log.getAttemptNumber() + 1);
        } else {
            log.setStatus(WebhookDeliveryStatus.FAILED);
            log.setNextRetryAt(null);
        }
    }

    private String truncate(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) return value;
        return value.substring(0, maxLength) + "...[truncated]";
    }
}
