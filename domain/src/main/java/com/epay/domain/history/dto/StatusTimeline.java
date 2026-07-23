package com.epay.domain.history.dto;

import com.epay.domain.history.enums.TransactionStatus;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.LinkedHashMap;

/**
 * Ordered map of status transitions for a transaction.
 * LinkedHashMap preserves insertion order so the timeline
 * is always returned in chronological sequence.
 */
@Data
@NoArgsConstructor
public class StatusTimeline {

    private LinkedHashMap<TransactionStatus, StatusEvent> statuses = new LinkedHashMap<>();

    public void add(TransactionStatus status, String actor, String message) {
        statuses.put(status, new StatusEvent(java.time.Instant.now(), actor, message));
    }

    public boolean has(TransactionStatus status) {
        return statuses.containsKey(status);
    }

    public StatusEvent get(TransactionStatus status) {
        return statuses.get(status);
    }

    public TransactionStatus latestStatus() {
        return statuses.isEmpty() ? null :
                statuses.keySet().stream()
                        .reduce((first, second) -> second)
                        .orElse(null);
    }
}
