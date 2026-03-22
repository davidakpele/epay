package com.example.admin_api_service.enums;

public enum SystemConfigScope {
    GLOBAL,         // Applies across the entire platform
    SERVICE,        // Applies to a specific microservice
    ENVIRONMENT     // Applies to a specific environment (PRODUCTION, STAGING)
}