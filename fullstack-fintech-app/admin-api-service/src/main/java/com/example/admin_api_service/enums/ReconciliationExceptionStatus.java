package com.example.admin_api_service.enums;

public enum ReconciliationExceptionStatus {
    OPEN,
    UNDER_INVESTIGATION,
    PENDING_ADJUSTMENT,     // Awaiting a manual adjustment to be applied
    RESOLVED,
    ESCALATED_TO_CHARGEBACK,
    WRITTEN_OFF,            // Variance accepted and written off
    CLOSED
}
