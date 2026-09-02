package com.epay.domain.deposit.enums;

public enum DepositStatus {
    PENDING,        // Created, awaiting user payment
    PROCESSING,     // Payment detected, awaiting gateway confirmation
    COMPLETED,      // Confirmed and wallet credited
    FAILED,         // Gateway returned failure
    CANCELLED,      // Expired or user-cancelled
    REFUNDED        // Refunded after failure
}
