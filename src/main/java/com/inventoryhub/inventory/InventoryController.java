package com.inventoryhub.inventory;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {
    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping("/{productId}")
    InventoryResponse getInventory(@PathVariable UUID productId) {
        return inventoryService.getInventory(productId);
    }

    @PostMapping("/reservations")
    ReservationResponse createReservation(@Valid @RequestBody CreateReservationRequest request) {
        return inventoryService.reserve(request);
    }

    @PostMapping("/reservations/{reservationId}/release")
    ReservationResponse releaseReservation(@PathVariable UUID reservationId) {
        return inventoryService.releaseReservation(reservationId);
    }
}
