# InventoryHub Implementation Status

## Completed

- Spring Boot application foundation with Maven.
- Catalog, order, inventory, notification, event, and shared API packages.
- REST APIs from the approved API contract.
- JPA persistence for products, orders, order lines, inventory items, reservations, outbox records, event processing records, and notification attempts.
- Order lifecycle implementation:
  - `OrderCreated`
  - `InventoryReserved`
  - `InventoryReservationFailed`
  - `OrderConfirmed`
  - `OrderCancelled`
  - `NotificationRequested`
- In-process event publisher for local MVP execution.
- Event envelope, outbox table, and idempotent consumer records.
- Structured API errors and correlation ID propagation.
- Structured JSON-style console logging.
- Spring Boot Actuator health and metrics endpoints.
- H2 default runtime for quick local execution.
- Docker Compose MySQL setup for production-like local persistence.
- Terraform scaffolding for IAM, SNS/SQS, RDS/MySQL, Lambda worker, and application configuration.
- Integration tests for primary catalog and order lifecycle behavior.

## Verified

Command:

```bash
mvn test
```

Result:

- 5 tests run.
- 0 failures.
- 0 errors.

## Intentionally Deferred

- Real AWS SNS/SQS adapters. The code currently uses in-process events while preserving the event envelope and persistence contracts.
- Flyway or Liquibase migrations. Hibernate DDL is used for the first MVP checkpoint.
- Real notification provider integration. Notification delivery is simulated.
- Deployable notification Lambda package. Terraform can create it once `enable_notification_lambda=true` and a package path is supplied.
- Final Spring Boot cloud deployment target. The architecture leaves ECS, Elastic Beanstalk, Lambda-only, or simple host deployment as an open decision.
- Public API authentication. Required before exposing write APIs publicly.
- CloudWatch dashboards and alarms beyond Terraform scaffolding.

## Traceability

- `ENG-001`: Completed.
- `ENG-002`: Completed for local H2 and Docker Compose MySQL.
- `ENG-003`: Completed for errors, validation, logging, and correlation IDs.
- `ENG-004`: Completed.
- `ENG-005`: Completed.
- `ENG-006`: Completed for core reservation and release behavior.
- `ENG-007`: Completed for event envelope, outbox records, and idempotency records.
- `ENG-008`: Completed locally through in-process event flow.
- `ENG-009`: Completed.
- `ENG-010`: Completed as simulated notification worker.
- `ENG-011`: Completed as Terraform scaffold; real apply requires AWS/account-specific review.
- `ENG-012`: Partially completed with Actuator and structured logs; CloudWatch alarms remain deferred.
- `ENG-013`: Partially completed with README and implementation status; full runbook remains future work.
