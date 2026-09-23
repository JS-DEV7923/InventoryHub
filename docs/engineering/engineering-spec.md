# InventoryHub Engineering Specification

Source: [InventoryHub PRD](../PRD.md)

## Architecture Overview

InventoryHub is composed of domain-oriented services for catalog, orders, inventory, and notifications. The MVP may start as separate Spring Boot deployables or a modular monolith with strict package boundaries; either approach must preserve the same domain ownership and API/event contracts.

Recommended default for implementation planning: begin with separate service modules in one repository so each domain can be tested independently while keeping local development manageable.

## Component Responsibilities

### Catalog Service

- Owns product metadata.
- Exposes product read APIs.
- Supplies product data required by order validation.
- Does not mutate inventory quantities.

### Order Service

- Owns order records, line items, totals, and lifecycle state.
- Validates create-order requests.
- Persists orders before publishing lifecycle events.
- Consumes inventory outcome events.
- Emits notification requests for customer-visible outcomes.

### Inventory Service

- Owns inventory item and reservation records.
- Processes `OrderCreated` events or direct reservation requests.
- Updates stock with transactional concurrency controls.
- Emits reservation success or failure events.
- Supports reservation release for cancellation or downstream failure.

### Notification Worker

- Consumes `NotificationRequested` events.
- Sends or simulates notification delivery.
- Records attempts.
- Retries transient failures and sends persistent failures to DLQ.

### Infrastructure

- Terraform provisions IAM, SQS/SNS, RDS/MySQL, Lambda worker resources, and application deployment resources.
- Environment-specific values must be supplied through variables, not hard-coded.

## Data Flow: Order Creation

1. Client calls `POST /api/orders`.
2. Order service validates syntax and business rules.
3. Order service loads product details from catalog data or catalog API.
4. Order service creates order and line items in `PENDING_RESERVATION`.
5. Order service publishes `OrderCreated`.
6. Inventory service consumes `OrderCreated`.
7. Inventory service reserves stock transactionally.
8. Inventory service publishes `InventoryReserved` or `InventoryReservationFailed`.
9. Order service consumes inventory result and updates status.
10. Order service publishes `OrderConfirmed` or `OrderCancelled`.
11. Order service publishes `NotificationRequested`.
12. Notification worker processes the request and records the result.

## State Machines

### Order Status

| State | Allowed Transitions | Trigger |
| --- | --- | --- |
| `PENDING_RESERVATION` | `CONFIRMED`, `CANCELLED` | Inventory outcome or cancel request |
| `CONFIRMED` | None for MVP | Successful reservation |
| `CANCELLED` | None for MVP | Reservation failure or cancel request |

### Reservation Status

| State | Allowed Transitions | Trigger |
| --- | --- | --- |
| `PENDING` | `RESERVED`, `FAILED` | Reservation attempt |
| `RESERVED` | `CONFIRMED`, `RELEASED` | Order confirmed or cancelled |
| `FAILED` | None for MVP | Insufficient stock or validation failure |
| `RELEASED` | None for MVP | Cancellation or cleanup |
| `CONFIRMED` | None for MVP | Finalized order |

## API Contract Expectations

### Error Response

All APIs should return a consistent error response:

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

### Event Envelope

All async messages must use the PRD event envelope. Consumers must reject malformed messages with structured logs and route unprocessable messages according to retry/DLQ policy.

## Persistence Expectations

- Use database migrations for all schema changes.
- Add indexes for lookup fields such as product ID, order ID, reservation order ID, event ID, and consumer name.
- Use optimistic locking or conditional update statements for inventory quantity changes.
- Store money values with fixed precision decimal columns.
- Persist event processing records before marking message processing complete.

## Error Handling

- Validation errors return HTTP 400.
- Missing resources return HTTP 404.
- Invalid state transitions return HTTP 409.
- Unexpected server errors return HTTP 500 without internal exception details.
- Transient downstream failures should be retried where safe.
- Consumers must log failed event IDs, correlation IDs, and root cause summaries.

## Edge Cases

- Duplicate create-order request with same idempotency key.
- Empty order line items.
- Duplicate product IDs in one order.
- Product becomes inactive before order submission.
- Inventory changes between order creation and reservation.
- Duplicate SQS delivery.
- Out-of-order lifecycle event delivery.
- DLQ message requiring manual replay.
- Notification failure after order confirmation.

## Testing Strategy

- Unit tests for validation, state machines, and service logic.
- Repository integration tests against MySQL-compatible test database.
- API tests for request/response contracts.
- Messaging integration tests with Localstack or AWS test resources.
- Concurrency tests for reservation race conditions.
- Terraform validation and plan checks.
- Smoke tests for health and metrics endpoints.

## Open Architecture Decisions

| Decision | Default Recommendation | Deadline |
| --- | --- | --- |
| Service deployment topology | Separate modules in one repo, deployable independently later | Before Milestone 1 completion |
| Messaging topology | SNS topic fan-out to SQS queues for lifecycle events | Before Milestone 4 |
| Notification runtime | Lambda worker for notification events | Before Milestone 5 |
| Authentication | Defer for MVP or add API key filter for deployed demos | Before public deployment |
| App deployment target | Choose simplest AWS target compatible with Spring Boot service deployment | Before Milestone 6 |
