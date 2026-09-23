package com.inventoryhub.orders;

import com.inventoryhub.events.EventEnvelope;
import com.inventoryhub.events.EventProcessingService;
import com.inventoryhub.events.EventType;
import com.inventoryhub.events.InventoryReservationFailedPayload;
import com.inventoryhub.events.InventoryReservedPayload;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class OrderInventoryEventHandler {
    private static final String CONSUMER_NAME = "order-service";

    private final OrderService orderService;
    private final EventProcessingService eventProcessingService;

    public OrderInventoryEventHandler(OrderService orderService, EventProcessingService eventProcessingService) {
        this.orderService = orderService;
        this.eventProcessingService = eventProcessingService;
    }

    @EventListener
    public void onEvent(EventEnvelope<?> envelope) {
        if (!EventType.INVENTORY_RESERVED.equals(envelope.eventType())
                && !EventType.INVENTORY_RESERVATION_FAILED.equals(envelope.eventType())) {
            return;
        }
        if (!eventProcessingService.markProcessedIfNew(envelope, CONSUMER_NAME)) {
            return;
        }

        if (EventType.INVENTORY_RESERVED.equals(envelope.eventType())) {
            InventoryReservedPayload payload = (InventoryReservedPayload) envelope.payload();
            orderService.confirmFromInventory(payload.orderId());
            return;
        }

        InventoryReservationFailedPayload payload = (InventoryReservationFailedPayload) envelope.payload();
        orderService.cancelFromInventory(payload.orderId(), payload.reason());
    }
}
