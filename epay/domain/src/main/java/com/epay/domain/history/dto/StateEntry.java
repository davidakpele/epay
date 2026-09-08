package com.epay.domain.history.dto;

import com.epay.domain.history.enums.TransactionStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * A single node in the state machine graph.
 *
 * <p>Fields:
 * <ul>
 *   <li>{@code timestamp}    — when this state was reached; null if never visited</li>
 *   <li>{@code actor}        — who triggered the transition; null if never visited</li>
 *   <li>{@code message}      — human-readable description; null if never visited</li>
 *   <li>{@code nextState}    — happy-path successor; null for terminal states</li>
 *   <li>{@code onFailure}    — true if this state failed and did NOT proceed to nextState;
 *                              false if the state completed successfully</li>
 *   <li>{@code failureState} — the state transitioned to when onFailure=true;
 *                              null when onFailure=false</li>
 * </ul>
 *
 * <p>Example — PROCESSING succeeded:
 * <pre>
 * "PROCESSING": {
 *   "timestamp": "…",
 *   "actor":     "SYSTEM",
 *   "message":   "Transaction validated and processing started",
 *   "nextState": "AUTHORIZED",
 *   "onFailure": false,
 *   "failureState": null
 * }
 * </pre>
 *
 * <p>Example — PROCESSING failed:
 * <pre>
 * "PROCESSING": {
 *   "timestamp": "…",
 *   "actor":     "SYSTEM",
 *   "message":   "Transaction validated and processing started",
 *   "nextState": "AUTHORIZED",
 *   "onFailure": true,
 *   "failureState": "FAILED"
 * }
 * </pre>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.ALWAYS)
public class StateEntry {

    /** UTC timestamp when this state was reached; null if never visited. */
    private Instant timestamp;

    /** Who triggered this transition (SYSTEM, PAYSTACK, LEDGER, …). */
    private String actor;

    /** Human-readable description of what happened. */
    private String message;

    /** The state this transitions to on the happy path; null for terminal states. */
    private TransactionStatus nextState;

    /**
     * Whether this state ended in failure.
     * {@code true}  → this state failed; failureState shows where it went.
     * {@code false} → this state completed successfully; it moved to nextState.
     */
    @Builder.Default
    private boolean onFailure = false;

    /**
     * The state transitioned to when {@code onFailure = true}.
     * Always null when {@code onFailure = false}.
     */
    private TransactionStatus failureState;
}
