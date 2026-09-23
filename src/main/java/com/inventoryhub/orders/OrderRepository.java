package com.inventoryhub.orders;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<CustomerOrder, UUID> {
    Optional<CustomerOrder> findByIdempotencyKey(String idempotencyKey);

    @EntityGraph(attributePaths = "items")
    Optional<CustomerOrder> findWithItemsById(UUID id);
}
