package com.inventoryhub.orders;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    CreateOrderResponse createOrder(
            @Valid @RequestBody CreateOrderRequest request,
            @RequestHeader(name = "Idempotency-Key", required = false) String idempotencyKey
    ) {
        return orderService.createOrder(request, idempotencyKey);
    }

    @GetMapping("/{orderId}")
    OrderResponse getOrder(@PathVariable UUID orderId) {
        return orderService.getOrder(orderId);
    }

    @PostMapping("/{orderId}/cancel")
    OrderResponse cancelOrder(@PathVariable UUID orderId) {
        return orderService.cancelOrder(orderId);
    }
}
