package com.inventoryhub.inventory;

import com.inventoryhub.common.ConflictException;
import com.inventoryhub.common.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class InventoryService {
    private final InventoryRepository inventoryRepository;
    private final ReservationRepository reservationRepository;

    public InventoryService(InventoryRepository inventoryRepository, ReservationRepository reservationRepository) {
        this.inventoryRepository = inventoryRepository;
        this.reservationRepository = reservationRepository;
    }

    @Transactional(readOnly = true)
    public InventoryResponse getInventory(UUID productId) {
        return inventoryRepository.findByProductId(productId)
                .map(InventoryResponse::from)
                .orElseThrow(() -> new NotFoundException("Inventory item not found"));
    }

    @Transactional(noRollbackFor = InsufficientStockException.class)
    public ReservationResponse reserve(CreateReservationRequest request) {
        List<InventoryItem> inventoryItems = new ArrayList<>();
        for (CreateReservationRequest.ReservationItemRequest item : request.items()) {
            InventoryItem inventory = inventoryRepository.findByProductIdForUpdate(item.productId())
                    .orElseThrow(() -> new NotFoundException("Inventory item not found"));
            if (inventory.getAvailableQuantity() < item.quantity()) {
                throw new InsufficientStockException(item.productId(), item.quantity(), inventory.getAvailableQuantity());
            }
            inventoryItems.add(inventory);
        }

        List<UUID> reservationIds = new ArrayList<>();
        for (int index = 0; index < request.items().size(); index++) {
            CreateReservationRequest.ReservationItemRequest item = request.items().get(index);
            InventoryItem inventory = inventoryItems.get(index);
            inventory.reserve(item.quantity());
            Reservation reservation = reservationRepository.save(new Reservation(
                    request.orderId(),
                    item.productId(),
                    item.quantity(),
                    ReservationStatus.RESERVED
            ));
            reservationIds.add(reservation.getId());
        }
        return new ReservationResponse(request.orderId(), reservationIds, ReservationStatus.RESERVED);
    }

    @Transactional
    public void releaseReservations(UUID orderId) {
        List<Reservation> reservations = reservationRepository.findByOrderIdAndStatus(orderId, ReservationStatus.RESERVED);
        for (Reservation reservation : reservations) {
            InventoryItem inventory = inventoryRepository.findByProductIdForUpdate(reservation.getProductId())
                    .orElseThrow(() -> new NotFoundException("Inventory item not found"));
            inventory.release(reservation.getQuantity());
            reservation.release();
        }
    }

    @Transactional
    public ReservationResponse releaseReservation(UUID reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new NotFoundException("Reservation not found"));
        if (reservation.getStatus() != ReservationStatus.RESERVED) {
            throw new ConflictException("Reservation cannot be released from status " + reservation.getStatus());
        }
        InventoryItem inventory = inventoryRepository.findByProductIdForUpdate(reservation.getProductId())
                .orElseThrow(() -> new NotFoundException("Inventory item not found"));
        inventory.release(reservation.getQuantity());
        reservation.release();
        return new ReservationResponse(reservation.getOrderId(), List.of(reservation.getId()), ReservationStatus.RELEASED);
    }
}
