package com.inventoryhub.events;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public class EventProcessingRecordId implements Serializable {
    private UUID eventId;
    private String consumerName;

    public EventProcessingRecordId() {
    }

    public EventProcessingRecordId(UUID eventId, String consumerName) {
        this.eventId = eventId;
        this.consumerName = consumerName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof EventProcessingRecordId that)) {
            return false;
        }
        return Objects.equals(eventId, that.eventId) && Objects.equals(consumerName, that.consumerName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(eventId, consumerName);
    }
}
