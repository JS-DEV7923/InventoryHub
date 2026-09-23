# InventoryHub Product Requirements Document

## 1. Overview

InventoryHub is an order and inventory microservices platform designed to demonstrate production-grade backend engineering with Java, Spring Boot, REST APIs, MySQL, AWS messaging, Lambda workers, and Terraform-managed infrastructure.

The system enables a customer-facing order flow backed by catalog lookup, inventory reservation, order state management, and asynchronous notifications. The product is intentionally scoped as an incremental portfolio-grade system: each milestone should be independently buildable, testable, deployable, and explainable.

## 2. Problem Statement

Modern commerce systems need to accept orders without overselling inventory, keep users informed about order progress, and remain reliable when downstream services are slow or unavailable. A single monolithic request path can become brittle when inventory checks, order persistence, payment-like workflows, and notifications all happen synchronously.

InventoryHub solves this by separating core responsibilities into services and using asynchronous messaging for order lifecycle events. This creates a realistic backend system where reliability patterns such as retries, dead-letter queues, health checks, structured logging, and cloud metrics are first-class requirements rather than afterthoughts.

## 3. Target Users

### Primary Users

- Backend engineers and reviewers evaluating the project architecture and implementation quality.
- Developers learning how to build Spring Boot microservices with AWS messaging and Terraform.
- Operations-minded engineers who need visibility into service health, failures, and event processing.

### Secondary Users

- Internal business users who need confidence that orders are accepted only when inventory can be reserved.
- Customer support users who need traceable order state and notification history.

## 4. Goals

- Model clear service boundaries for catalog, orders, inventory reservations, and notifications.
- Provide REST APIs for synchronous client actions such as browsing products, creating orders, and checking order status.
- Use AWS SQS/SNS for reliable asynchronous order lifecycle events.
- Prevent inventory overselling through explicit reservation workflows.
- Demonstrate production awareness through validation, retries, dead-letter queues, structured logs, health checks, and CloudWatch metrics.
- Provide Terraform modules for the required AWS infrastructure.
- Maintain a roadmap that supports incremental delivery and clear portfolio storytelling.

## 5. Non-Goals

- Full payment processing with a real payment gateway.
- Full user account management, authentication, or authorization in the first MVP.
- Multi-region active-active deployment.
- Advanced warehouse routing, procurement, returns, or shipment tracking.
- A polished consumer storefront UI.

## 6. Product Scope

### In Scope

- Product catalog APIs for listing and retrieving product details.
- Order APIs for creating orders and retrieving order status.
- Inventory reservation logic for reserving, confirming, and releasing stock.
- Event-driven communication for order created, inventory reserved, inventory failed, order confirmed, order cancelled, and notification requested events.
- Notification worker handling asynchronous notification events.
- MySQL persistence for catalog, orders, inventory, reservations, and event processing records.
- AWS infrastructure for SQS queues, SNS topics, RDS/MySQL, Lambda workers, IAM roles, and deployment resources.
- Observability through logs, metrics, alarms, and health endpoints.

### Out of Scope for MVP

- Payment capture and refunds.
- Admin UI.
- Real email/SMS provider integration.
- Distributed tracing beyond basic correlation IDs.
- Blue-green or canary release automation.

## 7. Assumptions

- Services will be implemented in Java with Spring Boot.
- MySQL will be used as the primary transactional database.
- AWS SQS/SNS will be used for asynchronous messaging.
- Lambda may be used for lightweight event workers, especially notification processing or operational jobs.
- Terraform will provision cloud infrastructure.
- Local development should support Docker Compose or equivalent tooling for MySQL and service dependencies.
- The system should favor understandable production patterns over excessive feature breadth.

## 8. User Journeys

### Browse Product Catalog

1. A client requests available products.
2. Catalog service returns product metadata, price, and availability summary.
3. The client selects products for an order.

### Create Order

1. A client submits an order request with customer contact details and line items.
2. Order service validates the request and creates an order in `PENDING_RESERVATION` status.
3. Order service publishes an order-created event.
4. Inventory service consumes the event and attempts to reserve stock.
5. Inventory service publishes either inventory-reserved or inventory-rejected event.
6. Order service updates the order to `CONFIRMED` or `CANCELLED`.
7. Notification worker sends or simulates a customer notification.

### Recover From Failed Processing

1. A message consumer fails because of a transient error.
2. The message is retried according to queue policy.
3. If processing continues to fail, the message is moved to a dead-letter queue.
4. Operators can inspect logs, metrics, and message metadata to diagnose the issue.

## 9. Service Boundaries

### Catalog Service

Owns product metadata and read-oriented product APIs.

Responsibilities:

- Create and expose product records.
- Provide product details needed by the order flow.
- Avoid owning inventory quantity or reservation state.

### Order Service

Owns order lifecycle and customer-facing order APIs.

Responsibilities:

- Validate order creation requests.
- Persist order records and line items.
- Publish order lifecycle events.
- Consume inventory outcome events.
- Maintain order state transitions.

### Inventory Service

Owns stock levels and reservation state.

Responsibilities:

- Track available inventory.
- Reserve stock for pending orders.
- Confirm or release reservations.
- Publish reservation outcome events.
- Prevent overselling under concurrent order attempts.

### Notification Service or Lambda Worker

Owns asynchronous customer/order notifications.

Responsibilities:

- Consume notification-requested events.
- Send or simulate email/SMS notifications.
- Record notification attempts and failures.
- Route repeated failures to a dead-letter queue.

## 10. Functional Requirements

### Catalog APIs

- The system must provide an API to list products.
- The system must provide an API to retrieve a product by ID.
- Product responses must include product ID, name, description, price, active status, and availability summary.
- Inactive products must not be orderable.

### Order APIs

- The system must provide an API to create an order.
- The create-order API must validate customer contact details, product IDs, quantities, and idempotency keys when available.
- The system must persist order status changes with timestamps.
- The system must provide an API to retrieve an order by ID.
- The order response must include order status, line items, reservation result when available, and relevant failure reason.

### Inventory Reservation

- Inventory reservation must be atomic for each product and quantity.
- The system must reject reservations when available stock is insufficient.
- The system must support reservation release when an order is cancelled or reservation confirmation fails downstream.
- Concurrent orders must not result in negative inventory.

### Messaging

- The system must publish order lifecycle events to SNS topics or SQS queues according to the chosen messaging topology.
- Consumers must process messages idempotently.
- Message payloads must include event ID, event type, timestamp, source service, correlation ID, and aggregate ID.
- Retry and dead-letter policies must be configured for each queue.

### Notifications

- The system must create notification requests for relevant order outcomes.
- Notification processing must be asynchronous.
- Notification failures must be logged and retried.
- Repeated notification failures must land in a dead-letter queue.

### Infrastructure

- Terraform must provision IAM roles and policies, queues, topics, RDS/MySQL, Lambda workers, and deployment resources.
- Terraform modules must be organized by infrastructure concern.
- Infrastructure configuration must support environment-specific values.
- Secrets and sensitive values must not be committed to source control.

## 11. Non-Functional Requirements

### Reliability

- APIs should return predictable validation errors for invalid client requests.
- Message consumers must tolerate duplicate messages.
- Transient failures must be retried with bounded retry policies.
- Dead-letter queues must exist for failed asynchronous workflows.

### Observability

- Services must emit structured JSON logs.
- Logs must include correlation IDs for request and event tracing.
- Health endpoints must expose service readiness and liveness.
- CloudWatch metrics must track API errors, message processing failures, queue depth, DLQ depth, and latency.

### Performance

- Catalog lookup APIs should respond within 300 ms locally for typical test data.
- Order creation should respond within 500 ms after persisting the pending order and publishing the event.
- Asynchronous reservation and notification processing should complete within 10 seconds under normal load.

### Security

- Services must validate all external inputs.
- Database credentials and cloud secrets must be supplied through environment variables or managed secret stores.
- IAM policies must follow least-privilege principles.
- Public APIs must avoid exposing internal exception details.

### Maintainability

- Services must use clear package boundaries for controllers, services, repositories, configuration, and messaging adapters.
- API contracts and event schemas must be documented.
- Tests must cover validation, state transitions, reservation concurrency, and message consumer idempotency.

## 12. Data Model

### Product

- `id`
- `sku`
- `name`
- `description`
- `price`
- `active`
- `created_at`
- `updated_at`

### Inventory Item

- `id`
- `product_id`
- `available_quantity`
- `reserved_quantity`
- `version`
- `updated_at`

### Order

- `id`
- `customer_email`
- `status`
- `total_amount`
- `failure_reason`
- `correlation_id`
- `created_at`
- `updated_at`

### Order Line

- `id`
- `order_id`
- `product_id`
- `quantity`
- `unit_price`

### Reservation

- `id`
- `order_id`
- `product_id`
- `quantity`
- `status`
- `created_at`
- `updated_at`

### Event Processing Record

- `event_id`
- `consumer_name`
- `status`
- `processed_at`
- `error_message`

## 13. Event Model

### Core Events

- `OrderCreated`
- `InventoryReserved`
- `InventoryReservationFailed`
- `OrderConfirmed`
- `OrderCancelled`
- `NotificationRequested`
- `NotificationSent`
- `NotificationFailed`

### Event Envelope

```json
{
  "eventId": "uuid",
  "eventType": "OrderCreated",
  "source": "order-service",
  "aggregateId": "order-id",
  "correlationId": "request-correlation-id",
  "occurredAt": "2026-09-23T00:00:00Z",
  "payload": {}
}
```

## 14. API Requirements

### Catalog

- `GET /api/products`
- `GET /api/products/{productId}`

### Orders

- `POST /api/orders`
- `GET /api/orders/{orderId}`
- `POST /api/orders/{orderId}/cancel`

### Inventory

- `GET /api/inventory/{productId}`
- `POST /api/inventory/reservations`
- `POST /api/inventory/reservations/{reservationId}/release`

### Operations

- `GET /actuator/health`
- `GET /actuator/metrics`

## 15. Success Metrics

- 100% of order creation requests produce a persisted order or a clear validation error.
- 0 confirmed orders with negative inventory during concurrency tests.
- 95% of asynchronous inventory decisions complete within 10 seconds in test environments.
- 100% of failed message-processing paths are retried and eventually succeed or move to a DLQ.
- All services expose health checks and structured logs with correlation IDs.
- Terraform can provision required infrastructure from a clean environment.

## 16. Acceptance Criteria

- A user can list products and retrieve product details through REST APIs.
- A user can create an order for products with available stock.
- The order transitions from pending to confirmed after successful inventory reservation.
- The order transitions from pending to cancelled when inventory is insufficient.
- Inventory cannot become negative under concurrent order attempts.
- Order lifecycle events are published and consumed asynchronously.
- Failed message processing uses retries and DLQs.
- Notification processing occurs asynchronously after order outcome events.
- Terraform modules exist for IAM, queues/topics, RDS/MySQL, Lambda workers, and application deployment.
- README documentation explains local setup, architecture, and incremental roadmap.

## 17. Milestone Roadmap

### Milestone 1: Project Foundation

- Create repository structure.
- Add Spring Boot service skeletons.
- Add shared conventions for logging, validation, and error responses.
- Add local MySQL setup.
- Add initial README roadmap.

### Milestone 2: Catalog and Order APIs

- Implement catalog APIs.
- Implement order creation and order retrieval.
- Persist orders and line items.
- Add validation and API tests.

### Milestone 3: Inventory Reservations

- Implement inventory data model.
- Add reservation workflow.
- Add concurrency-safe stock updates.
- Add tests for insufficient stock and concurrent orders.

### Milestone 4: Asynchronous Messaging

- Add event envelope.
- Publish order-created events.
- Consume inventory outcome events.
- Add retry and idempotency handling.

### Milestone 5: Notifications

- Implement notification worker or Lambda.
- Process notification-requested events.
- Record notification attempts.
- Add DLQ behavior for repeated failures.

### Milestone 6: Terraform and AWS Deployment

- Add Terraform modules for IAM, SQS/SNS, RDS/MySQL, Lambda, and app deployment.
- Add environment configuration.
- Document deployment steps.

### Milestone 7: Production Readiness

- Add structured logs and correlation IDs.
- Add health checks and metrics.
- Add CloudWatch alarms for queue depth, DLQ depth, and service errors.
- Add final architecture diagrams and operational runbook.

## 18. Risks and Mitigations

| Risk | Impact | Mitigation |
| --- | --- | --- |
| Overselling inventory under concurrency | Incorrect confirmed orders | Use transactional updates, optimistic locking, and concurrency tests |
| Duplicate message delivery | Repeated state changes or notifications | Use event IDs and idempotent consumers |
| Message processing failures | Orders stuck in intermediate states | Configure retries, DLQs, and operational alerts |
| Cloud cost surprises | Unexpected AWS spend | Use small instance sizes, lifecycle cleanup, and documented destroy steps |
| Complex scope creep | Incomplete portfolio project | Keep MVP focused on order lifecycle and production patterns |
| Missing observability | Hard-to-debug failures | Require structured logs, metrics, health checks, and correlation IDs |

## 19. Open Questions

- Should each domain run as a separate deployable service from the start, or should the project begin as a modular monolith and split later?
- Should SNS fan-out be used for all order lifecycle events, or should some events go directly through SQS queues?
- Should Lambda own notifications only, or should all consumers run as Spring Boot workers?
- Should authentication be deferred entirely or added as a lightweight API key layer?
- Should the first cloud deployment use ECS, Elastic Beanstalk, Lambda-only workers, or a simpler single-host deployment?

## 20. Launch Readiness Checklist

- REST APIs documented and tested.
- Database migrations committed and repeatable.
- Event schemas documented.
- Consumers are idempotent.
- Retries and DLQs configured.
- Health checks enabled.
- Structured logs include correlation IDs.
- CloudWatch dashboards and alarms configured.
- Terraform plan and apply verified in a test environment.
- README includes local setup, AWS deployment, and teardown instructions.
