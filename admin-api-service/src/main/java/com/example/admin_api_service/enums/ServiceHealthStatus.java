package com.example.admin_api_service.enums;

public enum ServiceHealthStatus {
    HEALTHY,
    DEGRADED,     // Responding but with elevated latency or partial failures
    UNHEALTHY,    // Failing health checks
    UNREACHABLE,  // No response from the service
    UNKNOWN       // Check could not be completed
}
 