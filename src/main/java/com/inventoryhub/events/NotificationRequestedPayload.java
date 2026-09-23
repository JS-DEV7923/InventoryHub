package com.inventoryhub.events;

import java.util.UUID;

public record NotificationRequestedPayload(UUID orderId, String customerEmail, String template) {
}
