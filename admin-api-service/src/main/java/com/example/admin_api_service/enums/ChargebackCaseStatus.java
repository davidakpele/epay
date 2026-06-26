package com.example.admin_api_service.enums;

public enum ChargebackCaseStatus {
    RAISED,
    UNDER_REVIEW,
    EVIDENCE_SUBMITTED,
    PRE_ARBITRATION,
    ARBITRATION,
    WON,                   // Chargeback ruled in our favour
    LOST,                  // Chargeback ruled against us
    REVERSED,              // Funds reversed to the cardholder
    CANCELLED,
    CLOSED
}
 
