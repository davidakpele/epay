package com.example.admin_api_service.enums;

public enum SessionTerminationReason {
    LOGOUT,
    EXPIRED,
    ADMIN_REVOKED,
    PASSWORD_CHANGED,
    TWO_FACTOR_REVOKED,
    SUSPICIOUS_ACTIVITY,
    IP_BLOCKED,
    FORCED_LOGOUT
}
