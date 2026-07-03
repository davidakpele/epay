package com.epay.common.events;

import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

/**
 * Base class for all internal domain events.
 * Events are published via Spring's {@link org.springframework.context.ApplicationEventPublisher}
 * and consumed by {@code @EventListener} methods in listener classes.
 */
@Getter
public abstract class DomainEvent {

    private final String eventId;
    private final Instant occurredAt;

    protected DomainEvent() {
        this.eventId    = UUID.randomUUID().toString();
        this.occurredAt = Instant.now();
    }
}
