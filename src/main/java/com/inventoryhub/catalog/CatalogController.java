package com.inventoryhub.catalog;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/products")
public class CatalogController {
    private final CatalogService catalogService;

    public CatalogController(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @GetMapping
    List<ProductResponse> listProducts() {
        return catalogService.listProducts();
    }

    @GetMapping("/{productId}")
    ProductResponse getProduct(@PathVariable UUID productId) {
        return catalogService.getProduct(productId);
    }
}
