package com.example.admin_api_service.enums;

public enum TransactionLimitType {
    SINGLE_TRANSACTION,     // Per-transaction cap
    DAILY,                  // Rolling 24-hour cumulative cap
    WEEKLY,                 // Rolling 7-day cumulative cap
    MONTHLY,                // Rolling 30-day cumulative cap
    COMPOSITE               // Combination enforced together (single + daily + monthly)
}