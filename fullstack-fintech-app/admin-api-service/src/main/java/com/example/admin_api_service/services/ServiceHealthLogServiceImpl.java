package com.example.admin_api_service.services;

import com.example.admin_api_service.Interfaces.IServiceHealthLogService;
import com.example.admin_api_service.enums.ServiceHealthStatus;
import com.example.admin_api_service.exceptions.ResourceNotFoundException;
import com.example.admin_api_service.models.systemAndConfiguration.ServiceHealthLog;
import com.example.admin_api_service.repository.ServiceHealthLogRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ServiceHealthLogServiceImpl implements IServiceHealthLogService {

    private static final int DEFAULT_RETENTION_DAYS = 30;
    private static final int ALERT_THRESHOLD_RESPONSE_MS = 5000;

    private final ServiceHealthLogRepository healthLogRepository;

    public ServiceHealthLogServiceImpl(ServiceHealthLogRepository healthLogRepository) {
        this.healthLogRepository = healthLogRepository;
    }

    @Override
    public ServiceHealthLog recordHealthCheck(String serviceName, String instanceId,
                                              String environment, ServiceHealthStatus status,
                                              Integer httpStatusCode, Long responseTimeMs,
                                              String healthPayload, String errorMessage,
                                              Double cpuUsagePercent, Double memoryUsagePercent,
                                              Integer dbConnectionCount) {
        ServiceHealthLog log = new ServiceHealthLog();
        log.setServiceName(serviceName);
        log.setInstanceId(instanceId);
        log.setEnvironment(environment);
        log.setStatus(status);
        log.setHttpStatusCode(httpStatusCode);
        log.setResponseTimeMs(responseTimeMs);
        log.setHealthPayload(healthPayload);
        log.setErrorMessage(errorMessage);
        log.setCpuUsagePercent(cpuUsagePercent);
        log.setMemoryUsagePercent(memoryUsagePercent);
        log.setDbConnectionCount(dbConnectionCount);
        log.setCheckedAt(LocalDateTime.now());

        // Flag log as alert-triggering if degraded/unhealthy or response is very slow
        boolean shouldAlert = status == ServiceHealthStatus.UNHEALTHY
                || status == ServiceHealthStatus.UNREACHABLE
                || (status == ServiceHealthStatus.DEGRADED)
                || (responseTimeMs != null && responseTimeMs > ALERT_THRESHOLD_RESPONSE_MS);
        log.setAlertTriggered(shouldAlert);

        return healthLogRepository.save(log);
    }

    @Override
    @Transactional(readOnly = true)
    public ServiceHealthLog getLogById(String logId) {
        return healthLogRepository.findById(logId)
                .orElseThrow(() -> new ResourceNotFoundException("ServiceHealthLog", "id", logId));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ServiceHealthLog> getAllLogs(Pageable pageable) {
        return healthLogRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ServiceHealthLog> getLogsByService(String serviceName, Pageable pageable) {
        return healthLogRepository.findAllByServiceNameOrderByCheckedAtDesc(serviceName, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ServiceHealthLog> getLogsByStatus(ServiceHealthStatus status, Pageable pageable) {
        return healthLogRepository.findAllByStatus(status, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ServiceHealthLog> getLatestStatusForService(String serviceName) {
        return healthLogRepository.findTopByServiceNameOrderByCheckedAtDesc(serviceName);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ServiceHealthLog> getAllLatestStatuses() {
        return healthLogRepository.findLatestPerService();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ServiceHealthLog> getRecentAlerts(int limitPerService) {
        return healthLogRepository.findAllByAlertTriggedTrueOrderByCheckedAtDesc(
                PageRequest.of(0, limitPerService, Sort.by("checkedAt").descending()));
    }

    @Override
    @Scheduled(cron = "0 0 2 * * *") // 2AM daily
    public void purgeOldLogs(int retentionDays) {
        int days = retentionDays > 0 ? retentionDays : DEFAULT_RETENTION_DAYS;
        LocalDateTime cutoff = LocalDateTime.now().minusDays(days);
        healthLogRepository.deleteAllByCheckedAtBefore(cutoff);
    }
}