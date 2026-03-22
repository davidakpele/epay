package com.example.admin_api_service.enums;

public enum ReconciliationExceptionType {
    MISSING_INTERNALLY,      // Transaction present externally but not in our ledger
    MISSING_EXTERNALLY,      // Transaction in our ledger but not in external statement
    AMOUNT_MISMATCH,         // Transaction found on both sides but amounts differ
    DATE_MISMATCH,           // Transaction found but posted on a different date
    DUPLICATE_INTERNAL,      // Duplicate transaction found in our ledger
    DUPLICATE_EXTERNAL,      // Duplicate transaction found in external statement
    STATUS_MISMATCH,         // Transaction statuses disagree between systems
    UNIDENTIFIED_CREDIT,     // Credit received with no matching internal record
    UNIDENTIFIED_DEBIT       // Debit recorded with no matching external record
}