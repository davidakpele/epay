package com.example.admin_api_service.Interfaces;

import com.example.admin_api_service.enums.AuditAction;

public interface IAuditLogService {
    void log(AuditAction action, Long adminId, String details, String entityType, String entityId);

    void logWithState(AuditAction action, Long adminId, String details, Object beforeState, Object afterState);

    void logSystemEvent(AuditAction action, String details);
}
