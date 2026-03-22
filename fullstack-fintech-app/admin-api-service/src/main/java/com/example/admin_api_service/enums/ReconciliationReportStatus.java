package com.example.admin_api_service.enums;

public enum ReconciliationReportStatus {
    PENDING,
    IN_PROGRESS,
    COMPLETED,           // Reconciled with zero exceptions
    COMPLETED_WITH_EXCEPTIONS,
    REVIEWED,
    CLOSED
}