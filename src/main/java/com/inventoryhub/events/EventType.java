package com.inventoryhub.events;

public final class EventType {
    public static final String ORDER_CREATED = "OrderCreated";
    public static final String INVENTORY_RESERVED = "InventoryReserved";
    public static final String INVENTORY_RESERVATION_FAILED = "InventoryReservationFailed";
    public static final String ORDER_CONFIRMED = "OrderConfirmed";
    public static final String ORDER_CANCELLED = "OrderCancelled";
    public static final String NOTIFICATION_REQUESTED = "NotificationRequested";
    public static final String NOTIFICATION_SENT = "NotificationSent";
    public static final String NOTIFICATION_FAILED = "NotificationFailed";

    private EventType() {
    }
}
