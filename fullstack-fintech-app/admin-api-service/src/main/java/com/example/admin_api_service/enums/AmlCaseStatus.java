package com.example.admin_api_service.enums;

public enum AmlCaseStatus {
    OPEN,
    UNDER_INVESTIGATION,
    PENDING_SAR,           // Awaiting Suspicious Activity Report filing
    SAR_FILED,
    ESCALATED,
    RESOLVED_CLEARED,      // Investigation concluded — no suspicious activity found
    RESOLVED_CONFIRMED,    // Suspicious activity confirmed — actioned
    CLOSED
}