package com.inventoryhub.events;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "event_outbox")
public class EventOutbox {
    @Id
    private UUID id;

    @Column(nullable = false, unique = true)
    private UUID eventId;

    @Column(nullable = false)
    private String eventType;

    @Column(nullable = false)
    private String aggregateId;

    @Column(nullable = false)
    private String correlationId;

    @Lob
    @Column(nullable = false)
    private String payload;

    @Column(nullable = false)
    private String status;

    @Column(nullable = false)
    private int publishAttempts;

    @Lob
    private String lastError;

    @Column(nullable = false)
    private Instant createdAt;

    private Instant publishedAt;

    protected EventOutbox() {
    }

    public EventOutbox(EventEnvelope<?> envelope, String payload) {
        this.id = UUID.randomUUID();
        this.eventId = envelope.eventId();
        this.eventType = envelope.eventType();
        this.aggregateId = envelope.aggregateId();
        this.correlationId = envelope.correlationId();
        this.payload = payload;
        this.status = "PENDING";
        this.publishAttempts = 0;
        this.createdAt = Instant.now();
    }

    public void markPublished() {
        this.status = "PUBLISHED";
        this.publishAttempts += 1;
        this.publishedAt = Instant.now();
    }
}
