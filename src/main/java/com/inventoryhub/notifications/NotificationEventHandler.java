package com.inventoryhub.notifications;

import com.inventoryhub.events.EventEnvelope;
import com.inventoryhub.events.EventProcessingService;
import com.inventoryhub.events.EventType;
import com.inventoryhub.events.NotificationRequestedPayload;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class NotificationEventHandler {
    private static final String CONSUMER_NAME = "notification-worker";

    private final EventProcessingService eventProcessingService;
    private final NotificationAttemptRepository notificationAttemptRepository;

    public NotificationEventHandler(
            EventProcessingService eventProcessingService,
            NotificationAttemptRepository notificationAttemptRepository
    ) {
        this.eventProcessingService = eventProcessingService;
        this.notificationAttemptRepository = notificationAttemptRepository;
    }

    @EventListener
    @Transactional
    public void onEvent(EventEnvelope<?> envelope) {
        if (!EventType.NOTIFICATION_REQUESTED.equals(envelope.eventType())) {
            return;
        }
        if (!eventProcessingService.markProcessedIfNew(envelope, CONSUMER_NAME)) {
            return;
        }

        NotificationRequestedPayload payload = (NotificationRequestedPayload) envelope.payload();
        notificationAttemptRepository.save(new NotificationAttempt(
                envelope.eventId(),
                payload.orderId(),
                payload.customerEmail(),
                payload.template(),
                "SIMULATED"
        ));
    }
}
