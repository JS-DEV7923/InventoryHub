# InventoryHub Component Design

## Catalog Service

- Responsibility: Own product metadata and product read APIs.
- Owned data: `products`.
- Dependencies: MySQL, shared error/logging conventions.
- APIs: `GET /api/products`, `GET /api/products/{productId}`.
- Scaling notes: Read-heavy; can scale horizontally behind the chosen deployment target.
- Reliability notes: Product reads should fail fast if database is unavailable.
- Failure modes: Missing product, inactive product, database timeout.
- Tests: API contract tests, repository tests, inactive product order validation through Order Service.

## Order Service

- Responsibility: Own order creation, state transitions, order lookup, cancellation, and lifecycle event publication.
- Owned data: `orders`, `order_lines`, order outbox rows, event processing records for order consumers.
- Dependencies: Catalog Service, MySQL, SNS/SQS, shared conventions.
- APIs: `POST /api/orders`, `GET /api/orders/{orderId}`, `POST /api/orders/{orderId}/cancel`.
- Scaling notes: Stateless HTTP layer; concurrent instances must rely on database constraints and idempotency keys.
- Reliability notes: Use transactional outbox to avoid losing events after order persistence.
- Failure modes: Product validation failure, duplicate idempotency key, event publishing failure, stale event, invalid state transition.
- Tests: API tests, state machine tests, outbox tests, idempotency tests, message consumer tests.

## Inventory Service

- Responsibility: Own stock quantities, reservations, release logic, and reservation outcome events.
- Owned data: `inventory_items`, `reservations`, event processing records for inventory consumers.
- Dependencies: MySQL, SNS/SQS, shared conventions.
- APIs: `GET /api/inventory/{productId}`, `POST /api/inventory/reservations`, `POST /api/inventory/reservations/{reservationId}/release`.
- Scaling notes: Multiple consumers may run, but database writes must use conditional updates or optimistic locking.
- Reliability notes: Consumer must be idempotent because SQS delivery is at least once.
- Failure modes: Insufficient stock, duplicate event, database lock conflict, malformed event, reservation release for terminal reservation.
- Tests: Concurrency tests, reservation workflow tests, duplicate event tests.

## Notification Worker

- Responsibility: Process `NotificationRequested` events and record notification attempts.
- Owned data: `notification_attempts`, event processing records for notification consumer.
- Dependencies: SQS event source, MySQL, CloudWatch, shared event envelope contract.
- Runtime: AWS Lambda in cloud; local implementation may be a simple worker adapter for tests.
- Scaling notes: Lambda concurrency should be bounded to protect the database.
- Reliability notes: Throw retriable errors for transient failures so SQS redrive policy can handle retries.
- Failure modes: Malformed notification event, database unavailable, simulated provider failure, duplicate delivery.
- Tests: Worker unit tests, queue integration tests, DLQ path tests.

## Shared Library

- Responsibility: Common event envelope, API errors, correlation IDs, logging fields, validation helpers, and message idempotency helpers.
- Owned data: None.
- Dependencies: Spring Boot, logging framework, JSON serializer.
- Reliability notes: Keep small and stable to avoid coupling domain logic across services.
- Failure modes: Incompatible event schema change, broken error response serialization.
- Tests: Contract tests and backward compatibility tests for event envelope.

## Terraform Modules

- Responsibility: Provision IAM, SNS/SQS, RDS/MySQL, Lambda, CloudWatch, and application deployment resources.
- Owned data: Terraform state, environment variable definitions, module outputs.
- Dependencies: AWS provider, selected backend for state, environment-specific variable files.
- Reliability notes: Defaults should be low-cost and teardown-friendly.
- Failure modes: Over-broad IAM, missing DLQ, unsafe public database exposure, expensive defaults.
- Tests: `terraform fmt`, `terraform validate`, plan review.
