package com.example.admin_api_service.enums;

public enum NotificationTemplateType {
    TRANSACTIONAL,      // Triggered by a specific user action or event
    ALERT,              // Compliance, fraud, or system alerts
    MARKETING,          // Promotional messages
    OTP,                // One-time password delivery
    SYSTEM,             // Platform-level notices e.g. maintenance, downtime
    REGULATORY,         // Mandatory regulatory notifications
    REMINDER,           // Scheduled reminders e.g. KYC expiry, pending approvals
    ONBOARDING          // New user / KYC onboarding flow messages
}
 
