package com.epay.domain.history.dto;

import com.epay.domain.history.enums.TransactionStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Full state-machine snapshot for a transaction.
 *
 * <p>Serialised fields (stored in DB + returned via API):
 * <ul>
 *   <li>{@code currentState} — the current/final status</li>
 *   <li>{@code states}       — every declared state node, keyed by TransactionStatus</li>
 *   <li>{@code allowedTransitions} — edges that were actually traversed</li>
 * </ul>
 *
 * <p>{@code failureTargets} is a pure runtime map — never serialised.
 * It is NOT a Lombok @Builder.Default field to avoid Jackson/Lombok conflicts.
 * It is initialised lazily inside {@link #declareState}.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StateMachine {

    /** The state the transaction is currently in. */
    private TransactionStatus currentState;

    /**
     * Every declared state for this transaction type, keyed by status.
     * States that were never reached have null timestamp/actor/message.
     * {@link LinkedHashMap} preserves canonical insertion order in JSON.
     */
    @Builder.Default
    private LinkedHashMap<TransactionStatus, StateEntry> states = new LinkedHashMap<>();

    /** Edges that were actually traversed, in order. */
    @Builder.Default
    private List<StateTransition> allowedTransitions = new ArrayList<>();

    // ── Runtime-only, never serialised ───────────────────────────────────────
    // NOT a @Builder.Default — avoids Lombok/Jackson $default$ method conflicts.
    // Initialised lazily in declareState().
    @JsonIgnore
    private transient LinkedHashMap<TransactionStatus, TransactionStatus> failureTargets;

    private LinkedHashMap<TransactionStatus, TransactionStatus> targets() {
        if (failureTargets == null) failureTargets = new LinkedHashMap<>();
        return failureTargets;
    }

    // ── Mutators ─────────────────────────────────────────────────────────────

    public void declareState(TransactionStatus status,
                             TransactionStatus nextState,
                             TransactionStatus failureTarget) {
        if (states == null) states = new LinkedHashMap<>();
        states.put(status, StateEntry.builder()
                .nextState(nextState)
                .onFailure(false)
                .failureState(null)
                .build());
        if (failureTarget != null) {
            targets().put(status, failureTarget);
        }
    }

    public void visitState(TransactionStatus status,
                           Instant timestamp,
                           String actor,
                           String message) {
        if (states == null) states = new LinkedHashMap<>();
        StateEntry entry = states.computeIfAbsent(status, k -> new StateEntry());
        entry.setTimestamp(timestamp);
        entry.setActor(actor);
        entry.setMessage(message);
        entry.setOnFailure(false);
        entry.setFailureState(null);
    }

    public void visitStateFailed(TransactionStatus status,
                                  Instant timestamp,
                                  String actor,
                                  String message) {
        if (states == null) states = new LinkedHashMap<>();
        StateEntry entry = states.computeIfAbsent(status, k -> new StateEntry());
        entry.setTimestamp(timestamp);
        entry.setActor(actor);
        entry.setMessage(message);
        entry.setOnFailure(true);
        entry.setFailureState(targets().getOrDefault(status, TransactionStatus.FAILED));
    }

    public void addTransition(TransactionStatus from, TransactionStatus to) {
        if (allowedTransitions == null) allowedTransitions = new ArrayList<>();
        allowedTransitions.add(StateTransition.builder().from(from).to(to).build());
    }
}
