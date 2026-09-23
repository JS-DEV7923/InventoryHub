package com.inventoryhub.events;

import java.util.List;
import java.util.UUID;

public record InventoryReservationFailedPayload(
        UUID orderId,
        String reason,
        List<FailedInventoryItemPayload> failedItems
) {
    public record FailedInventoryItemPayload(UUID productId, int requestedQuantity, int availableQuantity) {
    }
}
