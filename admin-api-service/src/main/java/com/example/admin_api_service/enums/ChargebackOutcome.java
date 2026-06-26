package com.example.admin_api_service.enums;

public enum ChargebackOutcome {
    WON,                    // Ruled in our favour — no funds returned
    LOST,                   // Ruled against us — funds returned to cardholder
    PARTIAL_WIN,            // Partial amount recovered
    ACCEPTED,               // We accepted the chargeback without dispute
    WRITTEN_OFF             // Loss accepted and written off
}
 