package com.inventoryhub.events;

import java.util.UUID;

public record OrderOutcomePayload(UUID orderId, String reason) {
}
