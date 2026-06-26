package com.example.admin_api_service.services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.admin_api_service.Interfaces.INotificationLogService;
import com.example.admin_api_service.Interfaces.INotificationTemplateService;
import com.example.admin_api_service.enums.NotificationChannel;
import com.example.admin_api_service.enums.NotificationLogStatus;
import com.example.admin_api_service.enums.NotificationPriority;
import com.example.admin_api_service.exceptions.BadRequestException;
import com.example.admin_api_service.exceptions.ConflictException;
import com.example.admin_api_service.exceptions.ResourceNotFoundException;
import com.example.admin_api_service.models.notificationsAndComms.NotificationLog;
import com.example.admin_api_service.models.notificationsAndComms.NotificationTemplate;
import com.example.admin_api_service.repository.NotificationLogRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
@Transactional
public class NotificationLogServiceImpl implements INotificationLogService {

    private static final int MAX_RETRIES = 3;

    private final NotificationLogRepository notificationLogRepository;
    private final INotificationTemplateService templateService;
    private final ObjectMapper objectMapper;

    public NotificationLogServiceImpl(NotificationLogRepository notificationLogRepository,
                                      INotificationTemplateService templateService,
                                      ObjectMapper objectMapper) {
        this.notificationLogRepository = notificationLogRepository;
        this.templateService = templateService;
        this.objectMapper = objectMapper;
    }

    @Override
    public NotificationLog send(String templateKey, NotificationChannel channel,
                                String locale, Long userId, Long walletId,
                                String recipient, String referenceId, String referenceType,
                                Map<String, String> variables, NotificationPriority priority) {

        NotificationTemplate template = templateService
                .getTemplateWithFallback(templateKey, channel, locale);

        String renderedBody    = templateService.renderBody(templateKey, channel, locale, variables);
        String renderedSubject = templateService.renderSubject(templateKey, channel, locale, variables);

        NotificationLog log = new NotificationLog();
        log.setTemplateId(template.getId());
        log.setTemplateKey(templateKey);
        log.setUserId(userId);
        log.setWalletId(walletId);
        log.setChannel(channel);
        log.setRecipient(recipient);
        log.setSubject(renderedSubject);
        log.setBody(renderedBody);
        log.setReferenceId(referenceId);
        log.setReferenceType(referenceType);
        log.setPriority(priority != null ? priority : NotificationPriority.NORMAL);
        log.setMaxRetries(MAX_RETRIES);
        log.setStatus(NotificationLogStatus.PENDING);
        log.setVariablesUsed(serializeVariables(variables));
        return notificationLogRepository.save(log);
    }

    @Override
    public NotificationLog sendAdHoc(NotificationChannel channel, Long userId,
                                     String recipient, String subject, String body,
                                     NotificationPriority priority, String referenceId,
                                     String referenceType) {
        NotificationLog log = new NotificationLog();
        log.setUserId(userId);
        log.setChannel(channel);
        log.setRecipient(recipient);
        log.setSubject(subject);
        log.setBody(body);
        log.setPriority(priority != null ? priority : NotificationPriority.NORMAL);
        log.setReferenceId(referenceId);
        log.setReferenceType(referenceType);
        log.setMaxRetries(MAX_RETRIES);
        log.setStatus(NotificationLogStatus.PENDING);
        return notificationLogRepository.save(log);
    }

    @Override
    public NotificationLog schedule(String templateKey, NotificationChannel channel,
                                    String locale, Long userId, String recipient,
                                    Map<String, String> variables, LocalDateTime scheduledAt) {

        NotificationTemplate template = templateService
                .getTemplateWithFallback(templateKey, channel, locale);

        String renderedBody    = templateService.renderBody(templateKey, channel, locale, variables);
        String renderedSubject = templateService.renderSubject(templateKey, channel, locale, variables);

        NotificationLog log = new NotificationLog();
        log.setTemplateId(template.getId());
        log.setTemplateKey(templateKey);
        log.setUserId(userId);
        log.setChannel(channel);
        log.setRecipient(recipient);
        log.setSubject(renderedSubject);
        log.setBody(renderedBody);
        log.setPriority(NotificationPriority.NORMAL);
        log.setMaxRetries(MAX_RETRIES);
        log.setStatus(NotificationLogStatus.PENDING);
        log.setScheduledAt(scheduledAt);
        log.setVariablesUsed(serializeVariables(variables));
        return notificationLogRepository.save(log);
    }

    @Override
    @Transactional(readOnly = true)
    public NotificationLog getLogById(String logId) {
        return notificationLogRepository.findById(logId)
                .orElseThrow(() -> new ResourceNotFoundException("NotificationLog", "id", logId));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NotificationLog> getAllLogs(Pageable pageable) {
        return notificationLogRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NotificationLog> getLogsByUser(Long userId, Pageable pageable) {
        return notificationLogRepository.findAllByUserId(userId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NotificationLog> getLogsByStatus(NotificationLogStatus status, Pageable pageable) {
        return notificationLogRepository.findAllByStatus(status, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NotificationLog> getLogsByChannel(NotificationChannel channel, Pageable pageable) {
        return notificationLogRepository.findAllByChannel(channel, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NotificationLog> getLogsByReference(String referenceId, Pageable pageable) {
        return notificationLogRepository.findAllByReferenceId(referenceId, pageable);
    }
    
    @Override
    public NotificationLog markDelivered(String logId, String providerReference) {
        NotificationLog log = getLogById(logId);
        log.setStatus(NotificationLogStatus.DELIVERED);
        log.setProviderReference(providerReference);
        log.setDeliveredAt(LocalDateTime.now());
        return notificationLogRepository.save(log);
    }

    @Override
    public NotificationLog markOpened(String logId) {
        NotificationLog log = getLogById(logId);
        log.setStatus(NotificationLogStatus.OPENED);
        log.setOpenedAt(LocalDateTime.now());
        return notificationLogRepository.save(log);
    }

    @Override
    public NotificationLog markFailed(String logId, String failureReason) {
        NotificationLog log = getLogById(logId);
        log.setFailureReason(failureReason);

        if (log.getRetryCount() >= log.getMaxRetries()) {
            log.setStatus(NotificationLogStatus.FAILED);
        } else {
            log.setRetryCount(log.getRetryCount() + 1);
            log.setStatus(NotificationLogStatus.PENDING);
        }
        return notificationLogRepository.save(log);
    }

    @Override
    public NotificationLog markBounced(String logId, String failureReason) {
        NotificationLog log = getLogById(logId);
        log.setStatus(NotificationLogStatus.BOUNCED);
        log.setFailureReason(failureReason);
        return notificationLogRepository.save(log);
    }

    @Override
    public NotificationLog retry(String logId) {
        NotificationLog log = getLogById(logId);
        if (log.getStatus() != NotificationLogStatus.FAILED) {
            throw new BadRequestException("Only FAILED notifications can be manually retried");
        }
        if (log.getRetryCount() >= log.getMaxRetries()) {
            throw new ConflictException("Maximum retries (" + log.getMaxRetries()
                    + ") already reached for this notification");
        }
        log.setStatus(NotificationLogStatus.PENDING);
        log.setRetryCount(log.getRetryCount() + 1);
        log.setFailureReason(null);
        return notificationLogRepository.save(log);
    }

    @Override
    public void cancelPending(String logId) {
        NotificationLog log = getLogById(logId);
        if (log.getStatus() != NotificationLogStatus.PENDING) {
            throw new ConflictException("Only PENDING notifications can be cancelled");
        }
        log.setStatus(NotificationLogStatus.CANCELLED);
        notificationLogRepository.save(log);
    }

    @Override
    @Scheduled(fixedDelay = 30_000) // every 30 seconds
    public void dispatchScheduledNotifications() {
        List<NotificationLog> due = notificationLogRepository
                .findAllByStatusAndScheduledAtBeforeOrEqual(
                        NotificationLogStatus.PENDING, LocalDateTime.now());
        due.forEach(log -> {
            log.setStatus(NotificationLogStatus.SENDING);
            log.setSentAt(LocalDateTime.now());
        });
        if (!due.isEmpty()) {
            notificationLogRepository.saveAll(due);
            // In production, publish each log to a message queue (Kafka/RabbitMQ)
            // for the actual sending worker to pick up
        }
    }

    private String serializeVariables(Map<String, String> variables) {
        if (variables == null || variables.isEmpty()) return null;
        try {
            return objectMapper.writeValueAsString(variables);
        } catch (JsonProcessingException e) {
            return null;
        }
    }
}