# InventoryHub Reliability Design

## Reliability Model

InventoryHub uses transactional persistence for local state and asynchronous messaging for cross-service workflows. The architecture assumes at-least-once message delivery and designs consumers to be idempotent.

## Key Failure Modes

| Failure Mode | Expected Behavior |
| --- | --- |
| Invalid API request | Return structured `400` response |
| Product not found | Return `404` or reject order with `400` depending on endpoint |
| Catalog unavailable during order creation | Fail order request safely before persistence |
| Order persisted but event publish fails | Outbox keeps event pending for retry |
| Duplicate SQS delivery | Consumer detects prior event processing and no-ops |
| Inventory lock conflict | Retry transaction within bounded policy or let message retry |
| Insufficient stock | Emit reservation failure and cancel order |
| Consumer repeatedly fails | Message moves to DLQ |
| Notification fails | Retry then DLQ; order state remains unchanged |
| Database unavailable | Health check fails; APIs return safe errors |

## Transactional Outbox

Order Service and Inventory Service should use an outbox table for events that must be published after database state changes.

Flow:

1. Write domain state and outbox event in the same transaction.
2. Background publisher reads pending outbox rows.
3. Publisher sends event to SNS.
4. Publisher marks outbox row as published.
5. Failed publishes remain pending or failed with attempt counts.

This directly mitigates the risk where a database commit succeeds but event publishing fails.

## Idempotency

- REST order creation uses `Idempotency-Key` when supplied.
- SQS consumers use `event_processing_records` keyed by `event_id` and `consumer_name`.
- Notification attempts use event ID uniqueness to prevent duplicate sends or duplicate simulated sends.
- State transitions must be guarded so stale events cannot move terminal orders.

## Timeouts

| Operation | Timeout Guidance |
| --- | --- |
| Catalog lookup during order creation | 500 ms |
| Database query | 1 to 3 seconds depending on operation |
| SQS poll | Long polling, up to 20 seconds |
| Lambda notification processing | Less than queue visibility timeout |

## Retries and DLQs

- API validation failures are not retried.
- Transient publishing failures are retried by outbox publisher.
- SQS processing failures use visibility timeout and redrive policy.
- Each main queue has a paired DLQ.
- DLQ depth greater than zero should alarm in deployed environments.

## Backpressure

- Queue depth indicates downstream backlog.
- Lambda concurrency should be bounded to avoid database overload.
- Spring Boot consumers should use configurable concurrency.
- API services should fail fast rather than hold requests open while async processing catches up.

## Recovery

- Pending outbox rows can be retried by restarting publisher workers.
- DLQ messages can be inspected manually; replay tooling is future work.
- Failed notification attempts do not roll back order state.
- Inventory correction should be manual for MVP if operational data is corrupted.

## SLO Assumptions

- Catalog API local p95 under 300 ms.
- Order creation local p95 under 500 ms after persistence and event enqueue/publish.
- Inventory decision p95 under 10 seconds in test environments.
- Zero confirmed orders with negative inventory.
