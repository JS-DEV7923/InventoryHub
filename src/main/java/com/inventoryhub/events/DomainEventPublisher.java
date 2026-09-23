package com.inventoryhub.events;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DomainEventPublisher {
    private final ApplicationEventPublisher applicationEventPublisher;
    private final EventOutboxRepository eventOutboxRepository;
    private final ObjectMapper objectMapper;

    public DomainEventPublisher(
            ApplicationEventPublisher applicationEventPublisher,
            EventOutboxRepository eventOutboxRepository,
            ObjectMapper objectMapper
    ) {
        this.applicationEventPublisher = applicationEventPublisher;
        this.eventOutboxRepository = eventOutboxRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public void publish(EventEnvelope<?> envelope) {
        EventOutbox outbox = eventOutboxRepository.save(new EventOutbox(envelope, toJson(envelope.payload())));
        applicationEventPublisher.publishEvent(envelope);
        outbox.markPublished();
    }

    private String toJson(Object payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException ex) {
            throw new IllegalArgumentException("Unable to serialize event payload", ex);
        }
    }
}
