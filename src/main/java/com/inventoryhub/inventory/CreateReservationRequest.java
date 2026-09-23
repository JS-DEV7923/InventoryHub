package com.inventoryhub.inventory;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;
import java.util.UUID;

public record CreateReservationRequest(
        @NotNull UUID orderId,
        @NotEmpty List<@Valid ReservationItemRequest> items
) {
    public record ReservationItemRequest(@NotNull UUID productId, @Positive int quantity) {
    }
}
