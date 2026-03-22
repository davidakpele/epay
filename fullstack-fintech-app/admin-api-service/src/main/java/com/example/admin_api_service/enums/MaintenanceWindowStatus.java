package com.example.admin_api_service.enums;

public enum MaintenanceWindowStatus {
    SCHEDULED,
    IN_PROGRESS,
    COMPLETED,
    CANCELLED,
    EXTENDED     // Actual end exceeded scheduled end
}