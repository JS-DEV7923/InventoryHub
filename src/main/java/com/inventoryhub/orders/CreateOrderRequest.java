package com.inventoryhub.orders;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;
import java.util.UUID;

public record CreateOrderRequest(
        @NotBlank @Email String customerEmail,
        @NotEmpty List<@Valid CreateOrderItemRequest> items
) {
    public record CreateOrderItemRequest(
            @NotNull UUID productId,
            @Positive int quantity
    ) {
    }
}
