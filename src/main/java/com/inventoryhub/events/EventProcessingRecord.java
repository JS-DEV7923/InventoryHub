package com.inventoryhub.events;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@IdClass(EventProcessingRecordId.class)
@Table(name = "event_processing_records")
public class EventProcessingRecord {
    @Id
    private UUID eventId;

    @Id
    private String consumerName;

    @Column(nullable = false)
    private String status;

    private Instant processedAt;

    @Lob
    private String errorMessage;

    protected EventProcessingRecord() {
    }

    public EventProcessingRecord(UUID eventId, String consumerName) {
        this.eventId = eventId;
        this.consumerName = consumerName;
        this.status = "PROCESSED";
        this.processedAt = Instant.now();
    }
}
