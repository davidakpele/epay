package com.example.admin_api_service.Interfaces;

import java.util.Map;
import java.time.LocalDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.example.admin_api_service.enums.NotificationChannel;
import com.example.admin_api_service.enums.NotificationLogStatus;
import com.example.admin_api_service.enums.NotificationPriority;
import com.example.admin_api_service.models.notificationsAndComms.NotificationLog;

public interface INotificationLogService {
  // Primary send — resolves template, renders body, and enqueues for dispatch
    NotificationLog send(String templateKey, NotificationChannel channel,
                         String locale, Long userId, Long walletId,
                         String recipient, String referenceId, String referenceType,
                         Map<String, String> variables, NotificationPriority priority);
 
    // Ad-hoc send without a template (e.g. custom admin messages)
    NotificationLog sendAdHoc(NotificationChannel channel, Long userId,
                               String recipient, String subject, String body,
                               NotificationPriority priority, String referenceId,
                               String referenceType);
 
    // Schedule a notification for future delivery
    NotificationLog schedule(String templateKey, NotificationChannel channel,
                              String locale, Long userId, String recipient,
                              Map<String, String> variables, LocalDateTime scheduledAt);
 
    NotificationLog getLogById(String logId);
 
    Page<NotificationLog> getAllLogs(Pageable pageable);
 
    Page<NotificationLog> getLogsByUser(Long userId, Pageable pageable);
 
    Page<NotificationLog> getLogsByStatus(NotificationLogStatus status, Pageable pageable);
 
    Page<NotificationLog> getLogsByChannel(NotificationChannel channel, Pageable pageable);
 
    Page<NotificationLog> getLogsByReference(String referenceId, Pageable pageable);
 
    // Called by the sending provider webhook to confirm delivery
    NotificationLog markDelivered(String logId, String providerReference);
 
    // Called when provider confirms recipient opened the email
    NotificationLog markOpened(String logId);
 
    NotificationLog markFailed(String logId, String failureReason);
 
    NotificationLog markBounced(String logId, String failureReason);
 
    // Retry a failed notification
    NotificationLog retry(String logId);
 
    void cancelPending(String logId);
 
    // Process scheduled notifications that are due
    void dispatchScheduledNotifications();
}
