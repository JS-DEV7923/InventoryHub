package com.inventoryhub.events;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record OrderCreatedPayload(
        UUID orderId,
        String customerEmail,
        List<OrderCreatedItemPayload> items
) {
    public record OrderCreatedItemPayload(UUID productId, int quantity, BigDecimal unitPrice) {
    }
}
