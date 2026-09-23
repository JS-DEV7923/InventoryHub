package com.inventoryhub.inventory;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "inventory_items")
public class InventoryItem {
    @Id
    private UUID id;

    @Column(nullable = false, unique = true)
    private UUID productId;

    @Column(nullable = false)
    private int availableQuantity;

    @Column(nullable = false)
    private int reservedQuantity;

    @Version
    private int version;

    @Column(nullable = false)
    private Instant updatedAt;

    protected InventoryItem() {
    }

    public InventoryItem(UUID id, UUID productId, int availableQuantity) {
        this.id = id;
        this.productId = productId;
        this.availableQuantity = availableQuantity;
        this.reservedQuantity = 0;
        this.updatedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public UUID getProductId() {
        return productId;
    }

    public int getAvailableQuantity() {
        return availableQuantity;
    }

    public int getReservedQuantity() {
        return reservedQuantity;
    }

    public void reserve(int quantity) {
        if (availableQuantity < quantity) {
            throw new InsufficientStockException(productId, quantity, availableQuantity);
        }
        availableQuantity -= quantity;
        reservedQuantity += quantity;
        updatedAt = Instant.now();
    }

    public void release(int quantity) {
        reservedQuantity -= quantity;
        availableQuantity += quantity;
        updatedAt = Instant.now();
    }
}
