package com.example.admin_api_service.enums;

public enum WebhookEndpointStatus {
    ACTIVE,
    INACTIVE,
    AUTO_DISABLED,      // Disabled automatically after consecutive failure threshold
    SUSPENDED           // Manually suspended by admin pending investigation
}
