package com.example.admin_api_service.services;

import com.example.admin_api_service.enums.AuditAction;
import com.example.admin_api_service.models.AuditLog;
import com.example.admin_api_service.repository.AuditLogRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;

import java.time.LocalDateTime;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final HttpServletRequest request; 
    private final ObjectMapper objectMapper;

    /**
     * Log an audit event
     */
    @Async
    public void log(AuditAction action, Long adminId, String details) {
        log(action, adminId, details, null, null);
    }

    /**
     * Log an audit event with entity information
     */
    @Async
    public void log(AuditAction action, Long adminId, String details, String entityType, String entityId) {
        AuditLog auditLog = AuditLog.builder()
                .action(action)
                .adminId(adminId)
                .details(details)
                .entityType(entityType)
                .entityId(entityId)
                .ipAddress(getClientIp())
                .userAgent(getUserAgent())
                .timestamp(LocalDateTime.now())
                .build();

        auditLogRepository.save(auditLog);
        log.debug("Audit log created: {} by admin {}", action, adminId);
    }

    /**
     * Log an audit event with before/after state
     */
    @Async
    public void logWithState(AuditAction action, Long adminId, String details, 
                            Object beforeState, Object afterState) {
        try {
            Map<String, Object> auditData = Map.of(
                "before", beforeState,
                "after", afterState,
                "timestamp", LocalDateTime.now()
            );

            AuditLog auditLog = AuditLog.builder()
                    .action(action)
                    .adminId(adminId)
                    .details(details)
                    .additionalData(objectMapper.writeValueAsString(auditData))
                    .ipAddress(getClientIp())
                    .userAgent(getUserAgent())
                    .timestamp(LocalDateTime.now())
                    .build();

            auditLogRepository.save(auditLog);
            
        } catch (JsonProcessingException e) {
            log.error("Failed to create audit log with state", e);
            // Fallback to simple log
            log(action, adminId, details);
        }
    }

    /**
     * Log system event (no admin)
     */
    @Async
    public void logSystemEvent(AuditAction action, String details) {
        AuditLog auditLog = AuditLog.builder()
                .action(action)
                .adminId(null) // system event
                .details(details)
                .ipAddress("SYSTEM")
                .userAgent("SYSTEM")
                .timestamp(LocalDateTime.now())
                .build();

        auditLogRepository.save(auditLog);
    }

    private String getClientIp() {
        if (request == null) return "UNKNOWN";
        
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        
        // Handle multiple IPs in X-Forwarded-For
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        
        return ip;
    }

    private String getUserAgent() {
        return request != null ? request.getHeader("User-Agent") : "UNKNOWN";
    }
}
