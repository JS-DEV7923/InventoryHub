package com.inventoryhub.notifications;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "notification_attempts")
public class NotificationAttempt {
    @Id
    private UUID id;

    @Column(nullable = false, unique = true)
    private UUID eventId;

    @Column(nullable = false)
    private UUID orderId;

    @Column(nullable = false)
    private String customerEmail;

    @Column(nullable = false)
    private String template;

    @Column(nullable = false)
    private String status;

    @Lob
    private String errorMessage;

    @Column(nullable = false)
    private Instant createdAt;

    protected NotificationAttempt() {
    }

    public NotificationAttempt(UUID eventId, UUID orderId, String customerEmail, String template, String status) {
        this.id = UUID.randomUUID();
        this.eventId = eventId;
        this.orderId = orderId;
        this.customerEmail = customerEmail;
        this.template = template;
        this.status = status;
        this.createdAt = Instant.now();
    }
}
