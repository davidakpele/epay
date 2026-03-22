package com.example.admin_api_service.enums;

public enum WebhookDeliveryStatus {
    PENDING,
    DELIVERING,
    SUCCESS,            // Endpoint returned 2xx
    FAILED,             // Non-2xx or timeout on final attempt
    RETRYING,           // Failed but retries remain
    CANCELLED,          // Endpoint was disabled before delivery completed
    SKIPPED             // Duplicate detected via idempotency key
}
 