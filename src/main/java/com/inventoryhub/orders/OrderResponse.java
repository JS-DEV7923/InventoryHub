package com.inventoryhub.orders;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record OrderResponse(
        UUID orderId,
        String customerEmail,
        OrderStatus status,
        BigDecimal totalAmount,
        String failureReason,
        List<OrderLineResponse> items,
        Instant createdAt,
        Instant updatedAt
) {
    static OrderResponse from(CustomerOrder order) {
        return new OrderResponse(
                order.getId(),
                order.getCustomerEmail(),
                order.getStatus(),
                order.getTotalAmount(),
                order.getFailureReason(),
                order.getItems().stream()
                        .map(line -> new OrderLineResponse(line.getProductId(), line.getQuantity(), line.getUnitPrice()))
                        .toList(),
                order.getCreatedAt(),
                order.getUpdatedAt()
        );
    }

    public record OrderLineResponse(UUID productId, int quantity, BigDecimal unitPrice) {
    }
}
