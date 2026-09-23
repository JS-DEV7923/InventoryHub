package com.inventoryhub.orders;

import com.inventoryhub.catalog.CatalogService;
import com.inventoryhub.catalog.Product;
import com.inventoryhub.common.ConflictException;
import com.inventoryhub.common.CorrelationId;
import com.inventoryhub.common.NotFoundException;
import com.inventoryhub.common.ValidationException;
import com.inventoryhub.events.DomainEventPublisher;
import com.inventoryhub.events.EventEnvelope;
import com.inventoryhub.events.EventType;
import com.inventoryhub.events.OrderCreatedPayload;
import com.inventoryhub.events.OrderOutcomePayload;
import com.inventoryhub.events.NotificationRequestedPayload;
import com.inventoryhub.inventory.InventoryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.UUID;

@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final CatalogService catalogService;
    private final InventoryService inventoryService;
    private final DomainEventPublisher eventPublisher;

    public OrderService(
            OrderRepository orderRepository,
            CatalogService catalogService,
            InventoryService inventoryService,
            DomainEventPublisher eventPublisher
    ) {
        this.orderRepository = orderRepository;
        this.catalogService = catalogService;
        this.inventoryService = inventoryService;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public CreateOrderResponse createOrder(CreateOrderRequest request, String idempotencyKey) {
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            var existing = orderRepository.findByIdempotencyKey(idempotencyKey);
            if (existing.isPresent()) {
                return CreateOrderResponse.from(existing.get());
            }
        }

        rejectDuplicateProducts(request);
        CustomerOrder order = new CustomerOrder(request.customerEmail(), CorrelationId.currentOrNew(), idempotencyKey);
        for (CreateOrderRequest.CreateOrderItemRequest item : request.items()) {
            Product product = catalogService.findProduct(item.productId());
            if (!product.isActive()) {
                throw new ValidationException("Inactive products cannot be ordered");
            }
            order.addLine(product.getId(), item.quantity(), product.getPrice());
        }

        CustomerOrder saved = orderRepository.save(order);
        CreateOrderResponse response = CreateOrderResponse.from(saved);
        eventPublisher.publish(EventEnvelope.create(
                EventType.ORDER_CREATED,
                "order-service",
                saved.getId().toString(),
                saved.getCorrelationId(),
                toOrderCreatedPayload(saved)
        ));
        return response;
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrder(UUID orderId) {
        return orderRepository.findWithItemsById(orderId)
                .map(OrderResponse::from)
                .orElseThrow(() -> new NotFoundException("Order not found"));
    }

    @Transactional
    public OrderResponse cancelOrder(UUID orderId) {
        CustomerOrder order = orderRepository.findWithItemsById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found"));
        try {
            order.cancel("CUSTOMER_CANCELLED");
        } catch (IllegalStateException ex) {
            throw new ConflictException("Order cannot be cancelled from status " + order.getStatus());
        }
        inventoryService.releaseReservations(orderId);
        eventPublisher.publish(EventEnvelope.create(
                EventType.ORDER_CANCELLED,
                "order-service",
                order.getId().toString(),
                order.getCorrelationId(),
                new OrderOutcomePayload(order.getId(), "CUSTOMER_CANCELLED")
        ));
        publishNotification(order, "ORDER_CANCELLED");
        return OrderResponse.from(order);
    }

    @Transactional
    public void confirmFromInventory(UUID orderId) {
        CustomerOrder order = orderRepository.findWithItemsById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found"));
        order.confirm();
        eventPublisher.publish(EventEnvelope.create(
                EventType.ORDER_CONFIRMED,
                "order-service",
                order.getId().toString(),
                order.getCorrelationId(),
                new OrderOutcomePayload(order.getId(), null)
        ));
        publishNotification(order, "ORDER_CONFIRMED");
    }

    @Transactional
    public void cancelFromInventory(UUID orderId, String reason) {
        CustomerOrder order = orderRepository.findWithItemsById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found"));
        try {
            order.cancel(reason);
        } catch (IllegalStateException ignored) {
            return;
        }
        eventPublisher.publish(EventEnvelope.create(
                EventType.ORDER_CANCELLED,
                "order-service",
                order.getId().toString(),
                order.getCorrelationId(),
                new OrderOutcomePayload(order.getId(), reason)
        ));
        publishNotification(order, "ORDER_CANCELLED");
    }

    private void publishNotification(CustomerOrder order, String template) {
        eventPublisher.publish(EventEnvelope.create(
                EventType.NOTIFICATION_REQUESTED,
                "order-service",
                order.getId().toString(),
                order.getCorrelationId(),
                new NotificationRequestedPayload(order.getId(), order.getCustomerEmail(), template)
        ));
    }

    private OrderCreatedPayload toOrderCreatedPayload(CustomerOrder order) {
        return new OrderCreatedPayload(
                order.getId(),
                order.getCustomerEmail(),
                order.getItems().stream()
                        .map(item -> new OrderCreatedPayload.OrderCreatedItemPayload(
                                item.getProductId(),
                                item.getQuantity(),
                                item.getUnitPrice()
                        ))
                        .toList()
        );
    }

    private void rejectDuplicateProducts(CreateOrderRequest request) {
        HashSet<UUID> productIds = new HashSet<>();
        boolean hasDuplicate = request.items().stream().anyMatch(item -> !productIds.add(item.productId()));
        if (hasDuplicate) {
            throw new ValidationException("Duplicate products in one order are not supported");
        }
    }
}
