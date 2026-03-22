package com.example.admin_api_service.enums;

public enum ReportExportStatus {
    PENDING,
    GENERATING,
    COMPLETED,
    FAILED,
    EXPIRED         // File has been deleted per retention policy
}
 