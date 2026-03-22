package com.example.admin_api_service.Interfaces;

import com.example.admin_api_service.enums.NotificationPriority;

public interface INotificationService {
    void notifyAdmins(NotificationPriority priority, String title, String message);
}
