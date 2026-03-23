package com.example.admin_api_service.Interfaces;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;
import com.example.admin_api_service.enums.ServiceHealthStatus;
import com.example.admin_api_service.models.systemAndConfiguration.ServiceHealthLog;

public interface IServiceHealthLogService {
    ServiceHealthLog recordHealthCheck(String serviceName, String instanceId,
                                       String environment, ServiceHealthStatus status,
                                       Integer httpStatusCode, Long responseTimeMs,
                                       String healthPayload, String errorMessage,
                                       Double cpuUsagePercent, Double memoryUsagePercent,
                                       Integer dbConnectionCount);
 
    ServiceHealthLog getLogById(String logId);
 
    Page<ServiceHealthLog> getAllLogs(Pageable pageable);
 
    Page<ServiceHealthLog> getLogsByService(String serviceName, Pageable pageable);
 
    Page<ServiceHealthLog> getLogsByStatus(ServiceHealthStatus status, Pageable pageable);
 
    // Latest status per service name
    Optional<ServiceHealthLog> getLatestStatusForService(String serviceName);
 
    List<ServiceHealthLog> getAllLatestStatuses();
 
    // Recent degraded or unhealthy logs
    List<ServiceHealthLog> getRecentAlerts(int limitPerService);
 
    // Purge logs older than retentionDays
    void purgeOldLogs(int retentionDays);
}
