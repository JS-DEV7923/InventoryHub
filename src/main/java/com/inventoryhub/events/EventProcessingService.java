package com.inventoryhub.events;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EventProcessingService {
    private final EventProcessingRecordRepository repository;

    public EventProcessingService(EventProcessingRecordRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public boolean markProcessedIfNew(EventEnvelope<?> envelope, String consumerName) {
        EventProcessingRecordId id = new EventProcessingRecordId(envelope.eventId(), consumerName);
        if (repository.existsById(id)) {
            return false;
        }
        repository.save(new EventProcessingRecord(envelope.eventId(), consumerName));
        return true;
    }
}
