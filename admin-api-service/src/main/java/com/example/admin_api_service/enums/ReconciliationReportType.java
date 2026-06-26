package com.example.admin_api_service.enums;

public enum ReconciliationReportType {
    DAILY,
    WEEKLY,
    MONTHLY,
    SETTLEMENT_BATCH,    // Tied to a specific SettlementBatch
    AD_HOC               // Manually triggered for a custom period
}
