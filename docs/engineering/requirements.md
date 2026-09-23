# InventoryHub Engineering Requirements

Source: [InventoryHub PRD](../PRD.md)

## PRD Validation Summary

The PRD is implementation-ready for an MVP order and inventory backend. The following ambiguities are non-blocking and should be resolved before cloud deployment hardening:

| ID | Ambiguity | Engineering Impact | Status |
| --- | --- | --- | --- |
| AMB-001 | Separate deployable services from day one versus modular monolith first | Affects repository layout, deployment topology, CI, and service discovery | Non-blocking for domain design |
| AMB-002 | SNS fan-out for all events versus direct SQS routing for some events | Affects Terraform modules, queue subscriptions, and event routing tests | Non-blocking for event envelope |
| AMB-003 | Lambda notifications only versus Spring Boot workers for all consumers | Affects packaging, local development, IAM, and worker observability | Non-blocking for notification contract |
| AMB-004 | Authentication deferred or lightweight API key | Affects API gateway, request filters, and security tests | Non-blocking for local MVP |
| AMB-005 | AWS deployment target for Spring Boot services | Affects Terraform app deployment resources | Non-blocking until deployment milestone |

## Requirement Traceability

| Req ID | Engineering Requirement | Source PRD Section | Testable Behavior |
| --- | --- | --- | --- |
| REQ-001 | Implement Catalog Service ownership of product metadata only | 9, 10 | Catalog APIs expose products without owning stock mutations |
| REQ-002 | Implement `GET /api/products` | 10, 14 | Returns active product list with required fields |
| REQ-003 | Implement `GET /api/products/{productId}` | 10, 14 | Returns one product or a typed 404 response |
| REQ-004 | Prevent inactive products from being ordered | 10 | Order creation rejects inactive products |
| REQ-005 | Implement Order Service ownership of order lifecycle | 9 | Order status changes are persisted with timestamps |
| REQ-006 | Implement `POST /api/orders` | 10, 14 | Valid requests create `PENDING_RESERVATION` orders |
| REQ-007 | Validate order payloads | 10, 11 | Invalid email, product IDs, quantities, or duplicate idempotency keys return clear errors |
| REQ-008 | Implement `GET /api/orders/{orderId}` | 10, 14 | Returns order, line items, status, and failure reason when present |
| REQ-009 | Implement `POST /api/orders/{orderId}/cancel` | 14 | Cancellable orders move to `CANCELLED` and release reservations when needed |
| REQ-010 | Persist order line items and order totals | 10, 12 | Order rows and line item rows are created transactionally |
| REQ-011 | Implement Inventory Service ownership of stock and reservations | 9 | Stock changes happen through inventory APIs or event consumers |
| REQ-012 | Implement atomic inventory reservation | 10 | Concurrent reservations cannot produce negative inventory |
| REQ-013 | Reject insufficient stock reservations | 10 | Reservation failure event is emitted with reason |
| REQ-014 | Support reservation release | 10, 14 | Reserved quantity decreases and availability is restored |
| REQ-015 | Implement event envelope | 13 | All events include event ID, type, source, aggregate ID, correlation ID, timestamp, and payload |
| REQ-016 | Publish order lifecycle events | 10, 13 | Order-created, confirmed, and cancelled events are emitted |
| REQ-017 | Consume inventory outcome events idempotently | 10, 11 | Duplicate inventory outcome events do not create duplicate state transitions |
| REQ-018 | Configure retries and DLQs for async workflows | 10, 11 | Failed messages are retried and then routed to DLQ |
| REQ-019 | Implement notification request processing | 9, 10 | Order outcome emits notification request; worker records attempts |
| REQ-020 | Persist event processing records | 12 | Consumer processing is traceable by event ID and consumer name |
| REQ-021 | Use MySQL for transactional persistence | 6, 7, 12 | Schema migrations create all required tables |
| REQ-022 | Provide Terraform modules for AWS resources | 10, 16 | Terraform includes IAM, SQS/SNS, RDS/MySQL, Lambda, and app deployment modules |
| REQ-023 | Externalize secrets and sensitive configuration | 10, 11 | No committed credentials; config is supplied through env vars or managed secrets |
| REQ-024 | Emit structured JSON logs with correlation IDs | 11, 20 | Logs include service, level, timestamp, correlation ID, event ID when applicable |
| REQ-025 | Expose health and metrics endpoints | 11, 14 | `/actuator/health` and `/actuator/metrics` are available |
| REQ-026 | Track CloudWatch operational metrics | 11, 20 | API errors, queue depth, DLQ depth, processing failures, and latency are observable |
| REQ-027 | Meet API performance targets in test environments | 11 | Catalog responds within 300 ms and order creation within 500 ms locally |
| REQ-028 | Meet async processing target | 11, 15 | 95% of inventory decisions complete within 10 seconds |
| REQ-029 | Document local setup, deployment, and teardown | 16, 20 | README has working setup and cleanup steps |
| REQ-030 | Cover validation, state transitions, concurrency, and idempotency with tests | 11 | Automated tests exist for required behavior |

## Requirement Priorities

| Priority | Requirements |
| --- | --- |
| P0 | REQ-001 through REQ-018, REQ-021, REQ-023, REQ-025, REQ-030 |
| P1 | REQ-019, REQ-020, REQ-022, REQ-024, REQ-026, REQ-027, REQ-028, REQ-029 |
| P2 | Additional deployment target hardening after AMB-005 is resolved |
