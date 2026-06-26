package com.example.admin_api_service.models.systemAndConfiguration;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

import com.example.admin_api_service.enums.ServiceHealthStatus;

@Entity
@Table(name = "service_health_logs")
public class ServiceHealthLog {

    @Id
    @Column(name = "id", length = 36, nullable = false, updatable = false)
    private String id = UUID.randomUUID().toString();

    // Name of the service being checked e.g. "payment-service", "kyc-service"
    @Column(name = "service_name", length = 100, nullable = false)
    private String serviceName;

    // Specific instance or pod identifier
    @Column(name = "instance_id", length = 100)
    private String instanceId;

    // Environment: PRODUCTION, STAGING, etc.
    @Column(name = "environment", length = 20, nullable = false)
    private String environment;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private ServiceHealthStatus status;

    // HTTP status code from the health check endpoint
    @Column(name = "http_status_code")
    private Integer httpStatusCode;

    // Response time in milliseconds
    @Column(name = "response_time_ms")
    private Long responseTimeMs;

    // Full health check response stored as JSON
    @Column(name = "health_payload", columnDefinition = "json")
    private String healthPayload;

    // Error detail if status is UNHEALTHY or DEGRADED
    @Column(name = "error_message", length = 1000)
    private String errorMessage;

    // CPU usage percentage at the time of check
    @Column(name = "cpu_usage_percent")
    private Double cpuUsagePercent;

    // Memory usage percentage at the time of check
    @Column(name = "memory_usage_percent")
    private Double memoryUsagePercent;

    // Active database connections at the time of check
    @Column(name = "db_connection_count")
    private Integer dbConnectionCount;

    // Whether this log entry triggered an alert to the on-call team
    @Column(name = "alert_triggered", nullable = false)
    private boolean alertTriggered = false;

    @Column(name = "checked_at", nullable = false)
    private LocalDateTime checkedAt = LocalDateTime.now();

    @Column(name = "created_on", nullable = false, updatable = false)
    private LocalDateTime createdOn = LocalDateTime.now();

    public ServiceHealthLog() {
    }

    public ServiceHealthLog(String id, String serviceName, String instanceId, String environment, ServiceHealthStatus status, Integer httpStatusCode, Long responseTimeMs, String healthPayload, String errorMessage, Double cpuUsagePercent, Double memoryUsagePercent, Integer dbConnectionCount, boolean alertTriggered, LocalDateTime checkedAt, LocalDateTime createdOn) {
        this.id = id;
        this.serviceName = serviceName;
        this.instanceId = instanceId;
        this.environment = environment;
        this.status = status;
        this.httpStatusCode = httpStatusCode;
        this.responseTimeMs = responseTimeMs;
        this.healthPayload = healthPayload;
        this.errorMessage = errorMessage;
        this.cpuUsagePercent = cpuUsagePercent;
        this.memoryUsagePercent = memoryUsagePercent;
        this.dbConnectionCount = dbConnectionCount;
        this.alertTriggered = alertTriggered;
        this.checkedAt = checkedAt;
        this.createdOn = createdOn;
    }

    // Getters & Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getServiceName() { return serviceName; }
    public void setServiceName(String serviceName) { this.serviceName = serviceName; }

    public String getInstanceId() { return instanceId; }
    public void setInstanceId(String instanceId) { this.instanceId = instanceId; }

    public String getEnvironment() { return environment; }
    public void setEnvironment(String environment) { this.environment = environment; }

    public ServiceHealthStatus getStatus() { return status; }
    public void setStatus(ServiceHealthStatus status) { this.status = status; }

    public Integer getHttpStatusCode() { return httpStatusCode; }
    public void setHttpStatusCode(Integer httpStatusCode) { this.httpStatusCode = httpStatusCode; }

    public Long getResponseTimeMs() { return responseTimeMs; }
    public void setResponseTimeMs(Long responseTimeMs) { this.responseTimeMs = responseTimeMs; }

    public String getHealthPayload() { return healthPayload; }
    public void setHealthPayload(String healthPayload) { this.healthPayload = healthPayload; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public Double getCpuUsagePercent() { return cpuUsagePercent; }
    public void setCpuUsagePercent(Double cpuUsagePercent) { this.cpuUsagePercent = cpuUsagePercent; }

    public Double getMemoryUsagePercent() { return memoryUsagePercent; }
    public void setMemoryUsagePercent(Double memoryUsagePercent) { this.memoryUsagePercent = memoryUsagePercent; }

    public Integer getDbConnectionCount() { return dbConnectionCount; }
    public void setDbConnectionCount(Integer dbConnectionCount) { this.dbConnectionCount = dbConnectionCount; }

    public boolean isAlertTriggered() { return alertTriggered; }
    public void setAlertTriggered(boolean alertTriggered) { this.alertTriggered = alertTriggered; }

    public LocalDateTime getCheckedAt() { return checkedAt; }
    public void setCheckedAt(LocalDateTime checkedAt) { this.checkedAt = checkedAt; }

    public LocalDateTime getCreatedOn() { return createdOn; }
    public void setCreatedOn(LocalDateTime createdOn) { this.createdOn = createdOn; }
}
