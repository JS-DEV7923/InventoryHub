package com.inventoryhub.catalog;

import java.math.BigDecimal;
import java.util.UUID;

public record ProductResponse(
        UUID id,
        String sku,
        String name,
        String description,
        BigDecimal price,
        boolean active,
        String availabilitySummary
) {
    static ProductResponse from(Product product, String availabilitySummary) {
        return new ProductResponse(
                product.getId(),
                product.getSku(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.isActive(),
                availabilitySummary
        );
    }
}
