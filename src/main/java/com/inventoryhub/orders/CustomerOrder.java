package com.inventoryhub.orders;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "orders")
public class CustomerOrder {
    @Id
    private UUID id;

    @Column(nullable = false)
    private String customerEmail;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    private String failureReason;

    @Column(nullable = false)
    private String correlationId;

    @Column(unique = true)
    private String idempotencyKey;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderLine> items = new ArrayList<>();

    protected CustomerOrder() {
    }

    public CustomerOrder(String customerEmail, String correlationId, String idempotencyKey) {
        this.id = UUID.randomUUID();
        this.customerEmail = customerEmail;
        this.status = OrderStatus.PENDING_RESERVATION;
        this.totalAmount = BigDecimal.ZERO;
        this.correlationId = correlationId;
        this.idempotencyKey = idempotencyKey;
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    public UUID getId() {
        return id;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public List<OrderLine> getItems() {
        return items;
    }

    public void addLine(UUID productId, int quantity, BigDecimal unitPrice) {
        OrderLine line = new OrderLine(this, productId, quantity, unitPrice);
        items.add(line);
        totalAmount = totalAmount.add(unitPrice.multiply(BigDecimal.valueOf(quantity)));
        updatedAt = Instant.now();
    }

    public void confirm() {
        if (status != OrderStatus.PENDING_RESERVATION) {
            return;
        }
        status = OrderStatus.CONFIRMED;
        failureReason = null;
        updatedAt = Instant.now();
    }

    public void cancel(String reason) {
        if (status != OrderStatus.PENDING_RESERVATION) {
            throw new IllegalStateException("Order is already terminal");
        }
        status = OrderStatus.CANCELLED;
        failureReason = reason;
        updatedAt = Instant.now();
    }
}
