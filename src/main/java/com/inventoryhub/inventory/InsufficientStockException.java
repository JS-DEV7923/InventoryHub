package com.inventoryhub.inventory;

import java.util.UUID;

public class InsufficientStockException extends RuntimeException {
    private final UUID productId;
    private final int requestedQuantity;
    private final int availableQuantity;

    public InsufficientStockException(UUID productId, int requestedQuantity, int availableQuantity) {
        super("Insufficient stock for product " + productId);
        this.productId = productId;
        this.requestedQuantity = requestedQuantity;
        this.availableQuantity = availableQuantity;
    }

    public UUID getProductId() {
        return productId;
    }

    public int getRequestedQuantity() {
        return requestedQuantity;
    }

    public int getAvailableQuantity() {
        return availableQuantity;
    }
}
