package com.inventoryhub.orders;

import java.util.UUID;

public record CreateOrderResponse(UUID orderId, OrderStatus status, String correlationId) {
    static CreateOrderResponse from(CustomerOrder order) {
        return new CreateOrderResponse(order.getId(), order.getStatus(), order.getCorrelationId());
    }
}
