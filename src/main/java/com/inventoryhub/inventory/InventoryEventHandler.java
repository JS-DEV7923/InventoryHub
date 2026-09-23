package com.inventoryhub.inventory;

import com.inventoryhub.events.DomainEventPublisher;
import com.inventoryhub.events.EventEnvelope;
import com.inventoryhub.events.EventProcessingService;
import com.inventoryhub.events.EventType;
import com.inventoryhub.events.InventoryReservationFailedPayload;
import com.inventoryhub.events.InventoryReservedPayload;
import com.inventoryhub.events.OrderCreatedPayload;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class InventoryEventHandler {
    private static final String CONSUMER_NAME = "inventory-service";

    private final InventoryService inventoryService;
    private final EventProcessingService eventProcessingService;
    private final DomainEventPublisher eventPublisher;

    public InventoryEventHandler(
            InventoryService inventoryService,
            EventProcessingService eventProcessingService,
            DomainEventPublisher eventPublisher
    ) {
        this.inventoryService = inventoryService;
        this.eventProcessingService = eventProcessingService;
        this.eventPublisher = eventPublisher;
    }

    @EventListener
    public void onEvent(EventEnvelope<?> envelope) {
        if (!EventType.ORDER_CREATED.equals(envelope.eventType())) {
            return;
        }
        if (!eventProcessingService.markProcessedIfNew(envelope, CONSUMER_NAME)) {
            return;
        }

        OrderCreatedPayload payload = (OrderCreatedPayload) envelope.payload();
        try {
            ReservationResponse reservation = inventoryService.reserve(new CreateReservationRequest(
                    payload.orderId(),
                    payload.items().stream()
                            .map(item -> new CreateReservationRequest.ReservationItemRequest(item.productId(), item.quantity()))
                            .toList()
            ));
            eventPublisher.publish(EventEnvelope.create(
                    EventType.INVENTORY_RESERVED,
                    "inventory-service",
                    payload.orderId().toString(),
                    envelope.correlationId(),
                    new InventoryReservedPayload(payload.orderId(), reservation.reservationIds())
            ));
        } catch (InsufficientStockException ex) {
            eventPublisher.publish(EventEnvelope.create(
                    EventType.INVENTORY_RESERVATION_FAILED,
                    "inventory-service",
                    payload.orderId().toString(),
                    envelope.correlationId(),
                    new InventoryReservationFailedPayload(
                            payload.orderId(),
                            "INSUFFICIENT_STOCK",
                            List.of(new InventoryReservationFailedPayload.FailedInventoryItemPayload(
                                    ex.getProductId(),
                                    ex.getRequestedQuantity(),
                                    ex.getAvailableQuantity()
                            ))
                    )
            ));
        } catch (RuntimeException ex) {
            eventPublisher.publish(EventEnvelope.create(
                    EventType.INVENTORY_RESERVATION_FAILED,
                    "inventory-service",
                    payload.orderId().toString(),
                    envelope.correlationId(),
                    new InventoryReservationFailedPayload(payload.orderId(), "RESERVATION_FAILED", List.of())
            ));
        }
    }
}
