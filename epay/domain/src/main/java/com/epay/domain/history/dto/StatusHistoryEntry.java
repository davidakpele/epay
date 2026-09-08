package com.epay.domain.history.dto;

import com.epay.domain.history.enums.TransactionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * One entry in the flat {@code statusHistory} list.
 *
 * <p>The list contains only the states that were actually visited,
 * in chronological order — making it easy to render a simple timeline
 * without walking the state-machine graph.
 *
 * <p>Example entry:
 * <pre>
 * {
 *   "status":    "AUTHORIZED",
 *   "timestamp": "2026-09-07T08:40:54.037005Z",
 *   "actor":     "PAYSTACK",
 *   "message":   "Payment authorized successfully"
 * }
 * </pre>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatusHistoryEntry {

    private TransactionStatus status;
    private Instant           timestamp;
    private String            actor;
    private String            message;
}
