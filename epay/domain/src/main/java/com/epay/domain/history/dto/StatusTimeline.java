package com.epay.domain.history.dto;

import com.epay.domain.history.enums.TransactionStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * The single JSON blob persisted in {@code transactions.status_timeline}.
 *
 * <p>It bundles two views of the same lifecycle:
 * <ul>
 *   <li>{@link #stateMachine} — the full graph: every declared state (visited or not),
 *       its {@code nextState}/{@code failureState} edges, and the complete
 *       {@code allowedTransitions} list.</li>
 *   <li>{@link #statusHistory} — the ordered list of states that were <em>actually</em>
 *       visited, suitable for rendering a simple timeline UI component.</li>
 * </ul>
 *
 * <p>Serialised JSON shape (stored in DB and returned via API):
 * <pre>
 * {
 *   "stateMachine": {
 *     "currentState": "DELIVERED",
 *     "states": { … },
 *     "allowedTransitions": [ … ]
 *   },
 *   "statusHistory": [
 *     { "status": "INITIATED",         "timestamp": "…", "actor": "SYSTEM",   "message": "…" },
 *     { "status": "PROCESSING",        "timestamp": "…", "actor": "SYSTEM",   "message": "…" },
 *     { "status": "AUTHORIZED",        "timestamp": "…", "actor": "PAYSTACK", "message": "…" },
 *     { "status": "SETTLEMENT_PENDING","timestamp": "…", "actor": "SYSTEM",   "message": "…" },
 *     { "status": "DELIVERED",         "timestamp": "…", "actor": "SYSTEM",   "message": "…" }
 *   ]
 * }
 * </pre>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatusTimeline {

    @Builder.Default
    private StateMachine stateMachine = new StateMachine();

    @Builder.Default
    private List<StatusHistoryEntry> statusHistory = new ArrayList<>();


    public void recordVisit(TransactionStatus status,
                            String actor,
                            String message) {
        Instant now = Instant.now();
        statusHistory.add(StatusHistoryEntry.builder()
                .status(status)
                .timestamp(now)
                .actor(actor)
                .message(message)
                .build());
        stateMachine.visitState(status, now, actor, message);
    }

    public void recordFailedVisit(TransactionStatus status,
                                   String actor,
                                   String message) {
        Instant now = Instant.now();
        statusHistory.add(StatusHistoryEntry.builder()
                .status(status)
                .timestamp(now)
                .actor(actor)
                .message(message)
                .build());
        stateMachine.visitStateFailed(status, now, actor, message);
    }

    @Deprecated(forRemoval = true)
    public void add(TransactionStatus status, String actor, String message) {
        recordVisit(status, actor, message);
    }

    @JsonIgnore
    public boolean has(TransactionStatus status) {
        return stateMachine.getStates().containsKey(status)
                && stateMachine.getStates().get(status).getTimestamp() != null;
    }

    @JsonIgnore
    public StatusHistoryEntry get(TransactionStatus status) {
        return statusHistory.stream()
                .filter(e -> e.getStatus() == status)
                .findFirst()
                .orElse(null);
    }

    @JsonIgnore
    public TransactionStatus latestStatus() {
        if (statusHistory.isEmpty()) return null;
        return statusHistory.get(statusHistory.size() - 1).getStatus();
    }
}
