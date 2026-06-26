package com.example.admin_api_service.enums;

public enum NotificationChannel {
    EMAIL,
    SMS,
    PUSH,           // Mobile push notification
    IN_APP,         // In-app notification bell
    WHATSAPP,
    WEBHOOK,        // Outbound webhook to partner/merchant
    SLACK,          // Internal Slack alert (ops/compliance team)
    USSD_FLASH      // USSD flash message
}