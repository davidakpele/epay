package com.example.admin_api_service.enums;

public enum ReportDeliveryChannel {
    EMAIL,
    STORAGE_ONLY,       // Saved to file storage with no notification
    EMAIL_AND_STORAGE,
    SFTP,               // Pushed to an SFTP server (regulatory submissions)
    WEBHOOK             // POSTed to a configured webhook endpoint
}