package com.inventoryhub.inventory;

import java.util.List;
import java.util.UUID;

public record ReservationResponse(UUID orderId, List<UUID> reservationIds, ReservationStatus status) {
}
