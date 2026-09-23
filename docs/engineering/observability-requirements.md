# InventoryHub Observability Requirements

Source: [InventoryHub PRD](../PRD.md)

## Logging

All services and workers must emit structured JSON logs with:

- timestamp
- level
- service name
- correlation ID
- request ID when applicable
- event ID when applicable
- aggregate ID when applicable
- message
- error type and safe error summary for failures

Do not log secrets, credentials, or full customer contact payloads.

## Metrics

| Metric | Component | Purpose |
| --- | --- | --- |
| API request count | All HTTP services | Traffic visibility |
| API latency | All HTTP services | Performance tracking |
| API error count by status | All HTTP services | Reliability tracking |
| Order created count | Order service | Business workflow volume |
| Order confirmed count | Order service | Successful outcome tracking |
| Order cancelled count | Order service | Failure outcome tracking |
| Reservation success/failure count | Inventory service | Inventory workflow health |
| Consumer processing failures | Event consumers | Async reliability tracking |
| Queue depth | SQS queues | Backlog detection |
| DLQ depth | SQS DLQs | Failed workflow detection |
| Notification success/failure count | Notification worker | Downstream worker health |

## Health Checks

- `/actuator/health` must expose liveness.
- Readiness must include database connectivity.
- Messaging readiness should verify required queue/topic configuration where practical.
- Health responses outside local development should avoid leaking sensitive implementation details.

## Alerts

| Alert | Trigger |
| --- | --- |
| API error rate high | Error threshold exceeded for sustained period |
| Queue backlog high | Queue depth above configured threshold |
| DLQ not empty | Any DLQ message count greater than zero for sustained period |
| Consumer failures high | Consumer processing failures exceed threshold |
| Database unavailable | Readiness check fails |
| Async latency high | Inventory decision p95 exceeds 10 seconds |

## Dashboards

Minimum dashboard panels:

- API request volume and latency by service.
- API error rates by service.
- Order created, confirmed, and cancelled counts.
- Reservation success and failure counts.
- Queue and DLQ depths.
- Consumer failure counts.
- Notification attempt outcomes.

## Debugging Hooks

- Correlation IDs must flow from API request to events and consumer logs.
- Event IDs must be searchable in logs and event processing records.
- DLQ messages must retain enough metadata for replay analysis.
- Order detail API must expose failure reason when inventory reservation fails.
