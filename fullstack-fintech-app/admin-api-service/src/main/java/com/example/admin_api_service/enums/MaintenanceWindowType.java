package com.example.admin_api_service.enums;

public enum MaintenanceWindowType {
    PLANNED,          // Scheduled, communicated in advance
    EMERGENCY,        // Unplanned, urgent intervention
    ROLLING_UPDATE,   // Zero-downtime deployment
    DATABASE_UPGRADE,
    INFRASTRUCTURE,
    SECURITY_PATCH
}