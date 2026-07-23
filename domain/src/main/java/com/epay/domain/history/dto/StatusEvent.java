package com.epay.domain.history.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Represents a single point in the transaction lifecycle.
 * Stored as part of the JSON statusTimeline in Transaction.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatusEvent {

    /** UTC timestamp when this status was reached. */
    private Instant timestamp;

    /**
     * Who/what triggered this status.
     * Examples: SYSTEM, PAYSTACK, FLUTTERWAVE, RULE_ENGINE, LEDGER, BANK, USER, ADMIN
     */
    private String actor;

    /** Human-readable description for this status transition. */
    private String message;
}
