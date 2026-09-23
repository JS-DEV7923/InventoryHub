package com.inventoryhub.events;

import java.util.List;
import java.util.UUID;

public record InventoryReservedPayload(UUID orderId, List<UUID> reservationIds) {
}
