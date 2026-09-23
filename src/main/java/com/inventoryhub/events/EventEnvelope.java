package com.inventoryhub.events;

import java.time.Instant;
import java.util.UUID;

public record EventEnvelope<T>(
        UUID eventId,
        String eventType,
        String source,
        String aggregateId,
        String correlationId,
        Instant occurredAt,
        int schemaVersion,
        T payload
) {
    public static <T> EventEnvelope<T> create(String eventType, String source, String aggregateId, String correlationId, T payload) {
        return new EventEnvelope<>(
                UUID.randomUUID(),
                eventType,
                source,
                aggregateId,
                correlationId,
                Instant.now(),
                1,
                payload
        );
    }
}
