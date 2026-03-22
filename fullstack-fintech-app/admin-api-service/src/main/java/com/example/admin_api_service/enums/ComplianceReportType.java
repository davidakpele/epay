package com.example.admin_api_service.enums;

public enum ComplianceReportType {
    SAR,                    // Suspicious Activity Report
    CTR,                    // Currency Transaction Report (large cash transactions)
    AML_SUMMARY,            // Monthly/quarterly AML activity summary
    KYC_AUDIT,              // KYC compliance audit report
    SANCTION_SCREENING,     // Sanction screening activity report
    RISK_ASSESSMENT,        // Periodic risk assessment report
    REGULATORY_RETURN,      // Scheduled regulatory filing (e.g. CBN returns)
    FRAUD_SUMMARY,          // Fraud incident summary
    TRANSACTION_MONITORING, // Transaction monitoring activity report
    CUSTOM                  // Ad-hoc or one-off report
}
 
