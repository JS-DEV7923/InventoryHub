package com.inventoryhub.inventory;

import java.util.UUID;

public record InventoryResponse(UUID productId, int availableQuantity, int reservedQuantity) {
    static InventoryResponse from(InventoryItem item) {
        return new InventoryResponse(item.getProductId(), item.getAvailableQuantity(), item.getReservedQuantity());
    }
}
