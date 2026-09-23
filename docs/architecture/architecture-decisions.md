# InventoryHub Architecture Decisions

## ADR-001: Use Separate Service Modules in One Repository

- Decision: Implement catalog, order, inventory, shared library, and notification worker as separate modules in one repository.
- Alternatives considered: Fully separate repositories; modular monolith only.
- Why chosen: It preserves service boundaries while keeping portfolio development, local testing, and cross-module refactoring manageable.
- Trade-offs: Modules can still become coupled if shared code grows too broad.
- Consequences: Build tooling must support multiple modules and clear package ownership.

## ADR-002: Use Spring Boot for Domain Services

- Decision: Catalog, order, and inventory services use Java with Spring Boot.
- Alternatives considered: Node.js, Go, Lambda-only services.
- Why chosen: The PRD explicitly calls for Java and Spring Boot, and Spring Boot provides validation, REST, Actuator, database, and messaging support.
- Trade-offs: Spring services are heavier than simple functions.
- Consequences: Deployment target must support long-running JVM services.

## ADR-003: Use MySQL/RDS as Transactional Store

- Decision: Use MySQL locally and RDS/MySQL in AWS.
- Alternatives considered: PostgreSQL, DynamoDB, in-memory stores.
- Why chosen: The PRD specifies MySQL and the reservation workflow benefits from relational transactions.
- Trade-offs: Cross-service database ownership must be carefully managed.
- Consequences: Migrations and indexes are required from the start.

## ADR-004: Use SNS Fan-Out to SQS Queues

- Decision: Publish domain events to SNS and deliver them to service-owned SQS queues.
- Alternatives considered: Direct SQS sends; Kafka; synchronous REST calls.
- Why chosen: SNS/SQS satisfies the AWS messaging requirement, supports durable fan-out, and keeps services decoupled.
- Trade-offs: More infrastructure than direct SQS.
- Consequences: Terraform must manage topics, subscriptions, queues, redrive policies, and IAM.

## ADR-005: Use Transactional Outbox for Required Events

- Decision: Persist events in an outbox table in the same transaction as domain state, then publish asynchronously.
- Alternatives considered: Publish directly after commit; distributed transactions.
- Why chosen: It mitigates the risk of saved order state without emitted events and avoids distributed transaction complexity.
- Trade-offs: Requires publisher logic and outbox monitoring.
- Consequences: Outbox metrics and retry behavior are part of production readiness.

## ADR-006: Use Idempotent Consumers

- Decision: Store event processing records keyed by event ID and consumer name.
- Alternatives considered: Rely on exactly-once delivery; deduplicate only in memory.
- Why chosen: SQS is at least once, so persistent idempotency is required.
- Trade-offs: Adds database writes for each consumed event.
- Consequences: Consumers must check and write processing records consistently.

## ADR-007: Use Conditional Inventory Updates or Optimistic Locking

- Decision: Inventory reservations must use database-level concurrency control.
- Alternatives considered: Application-level synchronized blocks; queue single-threading only.
- Why chosen: Database controls remain correct across multiple service instances and consumers.
- Trade-offs: Lock conflicts require retry or message retry handling.
- Consequences: Concurrency tests are mandatory before release.

## ADR-008: Use Lambda for Notification Worker

- Decision: Implement notification processing as a Lambda worker in the cloud path.
- Alternatives considered: Spring Boot notification service; inline notification processing.
- Why chosen: The PRD calls for Lambda coverage and notification work is naturally event-driven and lightweight.
- Trade-offs: Local parity requires a test adapter or Localstack setup.
- Consequences: Terraform must include Lambda package, role, event source mapping, and logs.

## ADR-009: Defer Authentication for Local MVP

- Decision: Local MVP does not require authentication, but public write APIs must be protected before deployment.
- Alternatives considered: Full user auth; API key from day one.
- Why chosen: The PRD excludes full user account management and keeps MVP focused on backend workflow.
- Trade-offs: Public deployment is blocked until API protection is added.
- Consequences: Security docs must clearly gate public exposure.

## ADR-010: Keep Real Email/SMS Out of MVP

- Decision: Notification delivery is simulated for MVP.
- Alternatives considered: SES, SNS SMS, SendGrid, Twilio.
- Why chosen: Real provider integration is out of scope and would distract from order/inventory reliability patterns.
- Trade-offs: Demo does not prove external provider delivery.
- Consequences: Notification attempts still record success/failure and DLQ behavior.
