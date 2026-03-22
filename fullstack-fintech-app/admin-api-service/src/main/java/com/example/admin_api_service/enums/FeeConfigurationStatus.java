package com.example.admin_api_service.enums;

public enum FeeConfigurationStatus {
    ACTIVE,
    INACTIVE,
    SCHEDULED,      // Created but effectiveFrom is in the future
    EXPIRED,        // effectiveTo has passed
    SUPERSEDED      // Replaced by a newer configuration
}
 
