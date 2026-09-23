# InventoryHub Observability Design

## Logging

All services and workers emit structured JSON logs with:

- `timestamp`
- `level`
- `service`
- `correlationId`
- `requestId`
- `eventId`
- `aggregateId`
- `message`
- `errorType`
- `errorSummary`

Log rules:

- Do not log secrets.
- Do not log full customer contact payloads.
- Log validation failures at info or warn level depending on severity.
- Log unexpected failures at error level with safe summaries.

## Correlation

- API gateway or service generates `X-Correlation-Id` if absent.
- Order Service stores correlation ID on the order.
- Event envelope carries correlation ID.
- Consumers include correlation ID in every log line.
- DLQ message metadata must preserve event ID and correlation ID.

## Metrics

| Metric | Component |
| --- | --- |
| `http_requests_total` | API services |
| `http_request_duration_ms` | API services |
| `http_errors_total` | API services |
| `orders_created_total` | Order Service |
| `orders_confirmed_total` | Order Service |
| `orders_cancelled_total` | Order Service |
| `inventory_reservations_succeeded_total` | Inventory Service |
| `inventory_reservations_failed_total` | Inventory Service |
| `message_processing_failures_total` | Consumers |
| `outbox_pending_count` | Publishing services |
| `notification_attempts_total` | Notification Worker |
| `notification_failures_total` | Notification Worker |
| SQS queue depth | CloudWatch |
| SQS DLQ depth | CloudWatch |

## Dashboards

Minimum dashboard sections:

- API traffic, latency, and errors by service.
- Order lifecycle counts.
- Reservation success and failure counts.
- Queue depth and DLQ depth.
- Outbox pending and failed rows.
- Notification attempt outcomes.
- Database health and connection failures.

## Alerts

| Alert | Condition |
| --- | --- |
| API error rate high | Error percentage exceeds configured threshold |
| DLQ not empty | Any DLQ has visible messages for sustained period |
| Queue backlog high | Main queue visible messages exceed threshold |
| Outbox stalled | Pending outbox rows remain unpublished beyond threshold |
| Database unavailable | Readiness check fails |
| Async latency high | Inventory decision p95 exceeds 10 seconds |

## Health Checks

- Liveness: application process is running.
- Readiness: database connection succeeds and required configuration is present.
- Messaging readiness: queue/topic URLs or ARNs are configured.
- Production health responses must hide internal details.

## Diagnostics

- Search by `orderId`, `correlationId`, or `eventId`.
- Event processing records show whether a consumer processed an event.
- Order detail exposes failure reason for inventory rejection.
- Notification attempts show outcome of simulated delivery.
