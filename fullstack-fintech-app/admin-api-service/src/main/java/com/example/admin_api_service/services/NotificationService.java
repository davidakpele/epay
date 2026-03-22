package com.example.admin_api_service.services;

import com.example.admin_api_service.Interfaces.INotificationService;
import com.example.admin_api_service.clients.EmailService;
import com.example.admin_api_service.enums.NotificationPriority;
import com.example.admin_api_service.models.AdminUser;
import com.example.admin_api_service.models.InAppNotification;
import com.example.admin_api_service.repository.AdminUserRepository;
import com.example.admin_api_service.repository.InAppNotificationRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.fasterxml.jackson.core.JsonProcessingException;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService implements INotificationService{

    private final AdminUserRepository adminUserRepository;
    private final InAppNotificationRepository inAppNotificationRepository;
    private final EmailService emailService;

    /**
     * Notify all admins with high priority notification
     */
    @Override
    public void notifyAdmins(NotificationPriority priority, String title, String message) {
        notifyAdmins(priority, title, message, null);
    }

    /**
     * Notify all admins with optional additional data
     */
    private void notifyAdmins(NotificationPriority priority, String title, String message, Object data) {
        List<AdminUser> admins = adminUserRepository.findByIsActiveTrue();

        for (AdminUser admin : admins) {
            createInAppNotification(admin, priority, title, message, data);
        }

        // For high priority, also send email
        if (priority == NotificationPriority.HIGH || priority == NotificationPriority.CRITICAL) {
            sendEmailToAdmins(priority, title, message, admins);
        }

        log.info("Notification sent to {} admins: {} - {}", admins.size(), title, message);
    }

    /**
     * Notify a specific admin
     */
    public void notifyAdmin(Long adminId, NotificationPriority priority, String title, String message) {
        adminUserRepository.findById(adminId).ifPresent(admin -> {
            createInAppNotification(admin, priority, title, message, null);

            if (priority == NotificationPriority.HIGH || priority == NotificationPriority.CRITICAL) {
                emailService.sendEmail(admin.getEmail(), title, message);
            }
        });
    }

    /**
     * Notify a specific admin with additional data
     */
    public void notifyAdmin(Long adminId, NotificationPriority priority, String title, String message, Object data) {
        adminUserRepository.findById(adminId).ifPresent(admin -> {
            createInAppNotification(admin, priority, title, message, data);
        });
    }

    private void createInAppNotification(AdminUser admin, NotificationPriority priority,  String title, String message, Object data) {
        InAppNotification notification = InAppNotification.builder()
                .adminId(admin.getId())
                .priority(priority)
                .title(title)
                .message(message)
                .data(data != null ? convertToJson(data) : null)
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();

        inAppNotificationRepository.save(notification);
    }

    private void sendEmailToAdmins(NotificationPriority priority, String title, String message, List<AdminUser> admins) {
        for (AdminUser admin : admins) {
            if (admin.getEmail() != null && admin.isNotificationEnabled()) {
                CompletableFuture.runAsync(() -> {
                    try {
                        emailService.sendEmail(admin.getEmail(), 
                            String.format("[%s] %s", priority, title), 
                            message);
                    } catch (Exception e) {
                        log.error("Failed to send email to admin {}: {}", admin.getId(), e.getMessage());
                    }
                });
            }
        }
    }

    private String convertToJson(Object data) {
        try {
            return new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(data);
        } catch (JsonProcessingException e) {
            log.error("Failed to convert notification data to JSON", e);
            return null;
        }
    }
}