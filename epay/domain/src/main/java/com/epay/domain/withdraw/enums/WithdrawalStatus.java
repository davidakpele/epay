package com.epay.domain.withdraw.enums;

public enum WithdrawalStatus {
    PENDING,        // Created, awaiting processing
    PROCESSING,     // Submitted to gateway
    COMPLETED,      // Successfully paid out
    FAILED,         // Gateway or validation failure
    REVERSED,       // Reversed after failure
    CANCELLED       // Cancelled by user or admin
}
