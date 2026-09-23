# InventoryHub Technical Architecture

Source documents:

- [PRD](../PRD.md)
- [Engineering Requirements](../engineering/requirements.md)
- [Engineering Specification](../engineering/engineering-spec.md)
- [Acceptance Criteria](../engineering/acceptance-criteria.md)
- [Technical Constraints](../engineering/technical-constraints.md)
- [Security Requirements](../engineering/security-requirements.md)
- [Observability Requirements](../engineering/observability-requirements.md)

## Architecture Summary

InventoryHub is a domain-oriented backend system for product catalog, order lifecycle, inventory reservation, and customer notifications. The recommended implementation is a single repository with separately buildable Spring Boot service modules and a Lambda-based notification worker. Services communicate through REST for client-facing APIs and AWS SNS/SQS for order lifecycle events.

The design intentionally favors understandable production patterns: clear data ownership, transactional inventory reservation, idempotent event consumers, bounded retries, dead-letter queues, structured logs, health checks, and Terraform-managed cloud resources.

## Goals

- Preserve service boundaries for catalog, orders, inventory, and notifications.
- Keep synchronous APIs fast and predictable.
- Move reservation and notification work onto durable asynchronous messaging.
- Prevent overselling under concurrent order attempts.
- Provide enough observability to diagnose API, database, and event-processing failures.
- Keep infrastructure reproducible through Terraform.

## Non-Goals

- Real payment processing.
- Full authentication or user account management in local MVP.
- Multi-region deployment.
- Advanced warehouse, shipping, returns, or procurement workflows.
- Consumer storefront UI.

## Assumptions

- Java and Spring Boot are used for catalog, order, and inventory services.
- MySQL is the transactional database.
- AWS SNS is used for domain event fan-out and SQS is used for durable service queues.
- Notification processing is implemented as an AWS Lambda worker for the cloud path.
- Local development can use Docker Compose for MySQL and Localstack for SQS/SNS.
- Public deployment requires API protection before write APIs are exposed.

## Actors and External Systems

| Actor or System | Role |
| --- | --- |
| API client | Lists products, creates orders, checks order state, requests cancellation |
| Developer | Runs services locally, tests flows, applies Terraform |
| Operator | Monitors logs, metrics, queues, DLQs, and health checks |
| AWS SNS | Publishes domain events to subscribers |
| AWS SQS | Stores durable work queues and DLQs |
| AWS Lambda | Runs notification worker in cloud |
| RDS/MySQL | Stores product, order, inventory, reservation, event, and notification data |
| CloudWatch | Stores logs, metrics, alarms, and dashboards |

## System Boundaries

| Boundary | Inside | Outside |
| --- | --- | --- |
| Public API boundary | Catalog and order REST APIs | API clients, future gateway/auth layer |
| Domain boundary | Catalog, order, inventory, notification modules | External payment/email/SMS providers are out of scope |
| Data ownership boundary | Each service owns its tables and writes | Other services may read only through APIs or events |
| Async boundary | SNS topics, SQS queues, DLQs, event envelope | Manual DLQ replay tooling is future work |
| Cloud trust boundary | IAM roles, RDS, queues, Lambda, logs | User machine, public internet, external clients |

## Runtime Components

| Component | Runtime | Primary Responsibility |
| --- | --- | --- |
| Catalog Service | Spring Boot | Product metadata APIs |
| Order Service | Spring Boot | Order creation, status, cancellation, lifecycle events |
| Inventory Service | Spring Boot worker/API | Stock and reservation ownership |
| Notification Worker | AWS Lambda | Notification request processing |
| Shared Library | Java module | Event envelope, errors, logging, correlation IDs |
| MySQL | Local MySQL or RDS | Transactional persistence |
| SNS/SQS | Localstack or AWS | Durable async messaging |
| Terraform | CLI/IaC | Reproducible infrastructure |

## Requirement to Component Mapping

| Requirement Area | Components |
| --- | --- |
| Catalog APIs | Catalog Service, MySQL |
| Order APIs | Order Service, Catalog Service, MySQL |
| Inventory reservations | Inventory Service, MySQL |
| Order lifecycle events | Order Service, Inventory Service, SNS, SQS |
| Notifications | Order Service, Notification Worker, SQS, MySQL |
| Validation and errors | Shared Library, all API services |
| Idempotency | Shared Library, event processing records, consumers |
| Retries and DLQs | SQS, consumers, Terraform |
| Health and metrics | Spring Boot Actuator, CloudWatch |
| Infrastructure | Terraform, IAM, RDS, SNS, SQS, Lambda |

## Primary Flow: Order Creation

1. Client sends `POST /api/orders` with customer email, line items, and optional idempotency key.
2. Order Service validates syntax and business rules.
3. Order Service verifies product details through Catalog Service or catalog module interface.
4. Order Service persists order and line items as `PENDING_RESERVATION`.
5. Order Service writes an outbox event for `OrderCreated`.
6. Event publisher publishes `OrderCreated` to SNS and marks the outbox record as published.
7. SNS fan-out delivers the event to the inventory reservation queue.
8. Inventory Service consumes the event idempotently.
9. Inventory Service reserves stock transactionally or records reservation failure.
10. Inventory Service publishes `InventoryReserved` or `InventoryReservationFailed`.
11. Order Service consumes the inventory outcome and updates order state.
12. Order Service publishes `OrderConfirmed` or `OrderCancelled`.
13. Order Service publishes `NotificationRequested`.
14. Notification Worker consumes the notification request, records an attempt, and simulates delivery.

## Consistency Model

- Order creation is strongly consistent inside the Order Service database transaction.
- Inventory reservation is eventually consistent relative to order creation.
- Order status is eventually consistent after inventory outcome events.
- Consumers use at-least-once delivery semantics and must be idempotent.
- Inventory quantity updates must be atomic and concurrency-safe.

## Quality Gate

- Every major requirement maps to one or more components.
- Components have clear ownership and responsibilities.
- API and event boundaries are defined in [API Contract](api-contract.md).
- Data ownership and storage are defined in [Data Model](data-model.md).
- Sync and async failure modes are covered in [Reliability Design](reliability-design.md).
- Authentication, authorization, secrets, and trust boundaries are covered in [Security Architecture](security-architecture.md).
- Observability is covered in [Observability Design](observability-design.md).
- Significant architectural decisions are recorded in [Architecture Decisions](architecture-decisions.md).
