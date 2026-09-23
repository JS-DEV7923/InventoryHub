# InventoryHub API and Event Contract

## API Conventions

- Base path: `/api`.
- Response format: JSON.
- Timestamps: ISO-8601 UTC.
- IDs: UUID strings unless implementation chooses database-generated numeric IDs consistently.
- Correlation ID: accept `X-Correlation-Id`; generate one when absent and return it in responses.
- Idempotency: accept `Idempotency-Key` for order creation.

## Error Response

```json
{
  "code": "VALIDATION_ERROR",
  "message": "Request validation failed",
  "correlationId": "uuid",
  "fieldErrors": [
    {
      "field": "items[0].quantity",
      "message": "must be greater than 0"
    }
  ]
}
```

## Catalog APIs

### `GET /api/products`

Response `200`:

```json
[
  {
    "id": "product-id",
    "sku": "SKU-001",
    "name": "Product name",
    "description": "Product description",
    "price": "19.99",
    "active": true,
    "availabilitySummary": "IN_STOCK"
  }
]
```

### `GET /api/products/{productId}`

- `200`: product detail.
- `404`: product not found.

## Order APIs

### `POST /api/orders`

Request:

```json
{
  "customerEmail": "customer@example.com",
  "items": [
    {
      "productId": "product-id",
      "quantity": 2
    }
  ]
}
```

Response `201`:

```json
{
  "orderId": "order-id",
  "status": "PENDING_RESERVATION",
  "correlationId": "uuid"
}
```

Errors:

- `400` for invalid email, empty items, inactive product, unknown product, or non-positive quantity.
- `409` for duplicate idempotency key with conflicting payload.
- `500` for unexpected server failure without internal details.

### `GET /api/orders/{orderId}`

Response `200`:

```json
{
  "orderId": "order-id",
  "customerEmail": "customer@example.com",
  "status": "CONFIRMED",
  "totalAmount": "39.98",
  "failureReason": null,
  "items": [
    {
      "productId": "product-id",
      "quantity": 2,
      "unitPrice": "19.99"
    }
  ],
  "createdAt": "2026-09-23T00:00:00Z",
  "updatedAt": "2026-09-23T00:00:03Z"
}
```

### `POST /api/orders/{orderId}/cancel`

- `200`: order cancelled.
- `404`: order not found.
- `409`: order already terminal or cannot be cancelled.

## Inventory APIs

### `GET /api/inventory/{productId}`

Response `200`:

```json
{
  "productId": "product-id",
  "availableQuantity": 10,
  "reservedQuantity": 2,
  "updatedAt": "2026-09-23T00:00:00Z"
}
```

### `POST /api/inventory/reservations`

Direct reservation API for testing and internal use.

Request:

```json
{
  "orderId": "order-id",
  "items": [
    {
      "productId": "product-id",
      "quantity": 2
    }
  ]
}
```

### `POST /api/inventory/reservations/{reservationId}/release`

- `200`: reservation released.
- `404`: reservation not found.
- `409`: reservation not releasable.

## Operations APIs

- `GET /actuator/health`
- `GET /actuator/metrics`

Production deployments should sanitize details and restrict administrative endpoints.

## Event Envelope

```json
{
  "eventId": "uuid",
  "eventType": "OrderCreated",
  "source": "order-service",
  "aggregateId": "order-id",
  "correlationId": "request-correlation-id",
  "occurredAt": "2026-09-23T00:00:00Z",
  "schemaVersion": 1,
  "payload": {}
}
```

## Event Types

### `OrderCreated`

Payload:

```json
{
  "orderId": "order-id",
  "customerEmail": "customer@example.com",
  "items": [
    {
      "productId": "product-id",
      "quantity": 2,
      "unitPrice": "19.99"
    }
  ]
}
```

### `InventoryReserved`

Payload:

```json
{
  "orderId": "order-id",
  "reservationIds": ["reservation-id"]
}
```

### `InventoryReservationFailed`

Payload:

```json
{
  "orderId": "order-id",
  "reason": "INSUFFICIENT_STOCK",
  "failedItems": [
    {
      "productId": "product-id",
      "requestedQuantity": 5,
      "availableQuantity": 1
    }
  ]
}
```

### `OrderConfirmed`

Payload:

```json
{
  "orderId": "order-id"
}
```

### `OrderCancelled`

Payload:

```json
{
  "orderId": "order-id",
  "reason": "INVENTORY_REJECTED"
}
```

### `NotificationRequested`

Payload:

```json
{
  "orderId": "order-id",
  "customerEmail": "customer@example.com",
  "template": "ORDER_CONFIRMED"
}
```

## Compatibility

- Event consumers must ignore unknown payload fields.
- Breaking schema changes require a new `schemaVersion`.
- Event names should remain stable once consumers are implemented.
- API changes should be additive for MVP unless the README and tests are updated together.
