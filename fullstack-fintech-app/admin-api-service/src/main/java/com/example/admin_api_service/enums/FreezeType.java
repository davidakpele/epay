package com.example.admin_api_service.enums;

public enum FreezeType {
    FULL,          // No debits or credits allowed
    DEBIT_ONLY,    // Debits blocked; credits still allowed
    CREDIT_ONLY    // Credits blocked; debits still allowed
}
 