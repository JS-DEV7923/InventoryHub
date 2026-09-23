# InventoryHub Acceptance Criteria

Source: [InventoryHub PRD](../PRD.md)

## Catalog

| AC ID | Criterion | Verification |
| --- | --- | --- |
| AC-001 | `GET /api/products` returns HTTP 200 with an array of product records | API test |
| AC-002 | Product list records include `id`, `sku`, `name`, `description`, `price`, `active`, and `availabilitySummary` | API contract test |
| AC-003 | `GET /api/products/{productId}` returns HTTP 200 for an existing product | API test |
| AC-004 | `GET /api/products/{productId}` returns HTTP 404 with structured error for missing product | API test |
| AC-005 | Inactive products cannot be used in `POST /api/orders` | Order validation test |

## Orders

| AC ID | Criterion | Verification |
| --- | --- | --- |
| AC-006 | Valid order creation returns HTTP 201 with order ID and `PENDING_RESERVATION` status | API and database test |
| AC-007 | Invalid email, empty line items, unknown product ID, or non-positive quantity returns HTTP 400 with field errors | API validation test |
| AC-008 | Order creation persists order and line items in one transaction | Repository integration test |
| AC-009 | `GET /api/orders/{orderId}` returns status, line items, reservation result when available, and failure reason when available | API contract test |
| AC-010 | Order transitions to `CONFIRMED` after successful inventory reservation event | Consumer integration test |
| AC-011 | Order transitions to `CANCELLED` after inventory failure event | Consumer integration test |
| AC-012 | Cancel endpoint cancels eligible orders and rejects terminal orders with HTTP 409 | API state transition test |

## Inventory

| AC ID | Criterion | Verification |
| --- | --- | --- |
| AC-013 | Reservation decreases available quantity and increases reserved quantity atomically | Repository integration test |
| AC-014 | Insufficient stock leaves quantities unchanged and emits failure outcome | Integration test |
| AC-015 | Concurrent reservation attempts never create negative available quantity | Concurrency test |
| AC-016 | Reservation release restores available quantity and marks reservation released | Integration test |

## Messaging

| AC ID | Criterion | Verification |
| --- | --- | --- |
| AC-017 | Published events use the required envelope fields | Contract test |
| AC-018 | Order creation publishes `OrderCreated` after successful order persistence | Integration test |
| AC-019 | Duplicate event IDs are processed once per consumer | Idempotency test |
| AC-020 | Failed message processing is retried according to queue policy | Localstack or AWS integration test |
| AC-021 | Messages that exceed retry policy move to DLQ | Localstack or AWS integration test |

## Notifications

| AC ID | Criterion | Verification |
| --- | --- | --- |
| AC-022 | Confirmed and cancelled orders trigger `NotificationRequested` | Integration test |
| AC-023 | Notification worker records notification attempts | Database test |
| AC-024 | Repeated notification failures route to notification DLQ | Queue integration test |

## Infrastructure and Operations

| AC ID | Criterion | Verification |
| --- | --- | --- |
| AC-025 | Terraform modules exist for IAM, SQS/SNS, RDS/MySQL, Lambda, and app deployment | Static repository check |
| AC-026 | Terraform plan succeeds with example environment variables | CI or local command |
| AC-027 | Services expose `/actuator/health` and `/actuator/metrics` | Smoke test |
| AC-028 | Structured logs include timestamp, level, service, correlation ID, and message | Log assertion test |
| AC-029 | CloudWatch metrics or metric definitions cover API errors, queue depth, DLQ depth, latency, and consumer failures | Terraform and runtime check |
| AC-030 | README documents local setup, deployment, teardown, and roadmap | Documentation review |
