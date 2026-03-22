package com.example.admin_api_service.enums;

public enum NotificationLogStatus {
    PENDING,        // Queued, not yet sent
    SENDING,        // Currently being dispatched
    SENT,           // Accepted by provider, delivery unconfirmed
    DELIVERED,      // Provider confirmed delivery
    OPENED,         // Recipient opened (email open tracking)
    FAILED,         // All attempts exhausted
    CANCELLED,      // Cancelled before sending (e.g. during maintenance)
    BOUNCED         // Provider confirmed non-deliverable address
}