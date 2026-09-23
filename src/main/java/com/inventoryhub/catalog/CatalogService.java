package com.inventoryhub.catalog;

import com.inventoryhub.common.NotFoundException;
import com.inventoryhub.inventory.InventoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class CatalogService {
    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;

    public CatalogService(ProductRepository productRepository, InventoryRepository inventoryRepository) {
        this.productRepository = productRepository;
        this.inventoryRepository = inventoryRepository;
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> listProducts() {
        return productRepository.findByActiveTrueOrderByNameAsc().stream()
                .map(product -> ProductResponse.from(product, availabilitySummary(product.getId())))
                .toList();
    }

    @Transactional(readOnly = true)
    public ProductResponse getProduct(UUID productId) {
        Product product = findProduct(productId);
        return ProductResponse.from(product, availabilitySummary(product.getId()));
    }

    @Transactional(readOnly = true)
    public Product findProduct(UUID productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException("Product not found"));
    }

    private String availabilitySummary(UUID productId) {
        return inventoryRepository.findByProductId(productId)
                .map(item -> item.getAvailableQuantity() > 0 ? "IN_STOCK" : "OUT_OF_STOCK")
                .orElse("UNKNOWN");
    }
}
