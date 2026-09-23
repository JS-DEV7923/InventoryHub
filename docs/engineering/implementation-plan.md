# InventoryHub Implementation Plan

Source: [InventoryHub PRD](../PRD.md)

## Task Sequence

### ENG-001: Establish Repository Foundation

- Objective: Create the baseline project structure.
- Description: Add service/module directories, shared conventions, build configuration, formatting, and README skeleton.
- Dependencies: None.
- Affected components: Repository, build tooling, documentation.
- Expected behavior: Developers can build and run tests from a clean checkout.
- Acceptance criteria: Build command succeeds; README explains project layout.
- Tests required: Build smoke test.
- Definition of done: Project skeleton committed with documented commands.

### ENG-002: Add Local Development Environment

- Objective: Provide local MySQL and service configuration.
- Description: Add Docker Compose or equivalent local setup, sample environment variables, and startup documentation.
- Dependencies: ENG-001.
- Affected components: Local infra, configuration, README.
- Expected behavior: Developer can start MySQL locally and connect services.
- Acceptance criteria: Local database starts; documented connection values work.
- Tests required: Database connectivity smoke test.
- Definition of done: Local setup reproducible from README.

### ENG-003: Implement Shared API and Logging Conventions

- Objective: Standardize validation, errors, correlation IDs, and structured logs.
- Description: Add shared error response model, exception handling, correlation ID propagation, and JSON logging setup.
- Dependencies: ENG-001.
- Affected components: Shared library or service common packages.
- Expected behavior: APIs return consistent errors and logs include correlation IDs.
- Acceptance criteria: Validation and exception tests pass.
- Tests required: Unit and API error tests.
- Definition of done: Shared convention adopted by initial services.

### ENG-004: Implement Catalog Service

- Objective: Provide product metadata APIs.
- Description: Add product schema, repository, service logic, and `GET /api/products` plus `GET /api/products/{productId}`.
- Dependencies: ENG-002, ENG-003.
- Affected components: Catalog service, database.
- Expected behavior: Clients can list and retrieve products.
- Acceptance criteria: AC-001 through AC-005.
- Tests required: Repository, API, and validation tests.
- Definition of done: Catalog APIs documented and tested.

### ENG-005: Implement Order Service Core

- Objective: Create and retrieve orders.
- Description: Add order and line item schema, create-order validation, total calculation, persistence, and order detail API.
- Dependencies: ENG-003, ENG-004.
- Affected components: Order service, database, catalog integration.
- Expected behavior: Valid orders persist in `PENDING_RESERVATION`.
- Acceptance criteria: AC-006 through AC-009.
- Tests required: API, repository, and validation tests.
- Definition of done: Order creation and retrieval work locally with test data.

### ENG-006: Implement Inventory Reservation Core

- Objective: Reserve and release inventory safely.
- Description: Add inventory and reservation schema, atomic reservation logic, insufficient stock handling, and release behavior.
- Dependencies: ENG-002, ENG-004.
- Affected components: Inventory service, database.
- Expected behavior: Stock cannot become negative, including under concurrency.
- Acceptance criteria: AC-013 through AC-016.
- Tests required: Integration and concurrency tests.
- Definition of done: Reservation workflow passes concurrency checks.

### ENG-007: Add Event Envelope and Messaging Abstractions

- Objective: Standardize async messaging contracts.
- Description: Add event envelope model, event serialization, publisher interface, consumer base behavior, and event processing record persistence.
- Dependencies: ENG-003.
- Affected components: Shared messaging, database.
- Expected behavior: Events are published and consumed with consistent metadata and idempotency hooks.
- Acceptance criteria: AC-017, AC-019.
- Tests required: Contract and idempotency tests.
- Definition of done: Shared event contract used by order and inventory workflows.

### ENG-008: Wire Order and Inventory Async Workflow

- Objective: Connect order creation to inventory reservation through events.
- Description: Publish `OrderCreated`, consume it in inventory, publish inventory outcome, and update order state from outcome.
- Dependencies: ENG-005, ENG-006, ENG-007.
- Affected components: Order service, inventory service, queues/topics.
- Expected behavior: Orders move from pending to confirmed or cancelled asynchronously.
- Acceptance criteria: AC-010, AC-011, AC-018, AC-020, AC-021.
- Tests required: Messaging integration tests.
- Definition of done: End-to-end order lifecycle works locally with async messaging.

### ENG-009: Implement Order Cancellation

- Objective: Support order cancellation and reservation release.
- Description: Add cancel endpoint, state transition checks, reservation release event or direct release flow.
- Dependencies: ENG-005, ENG-006, ENG-008.
- Affected components: Order service, inventory service.
- Expected behavior: Eligible orders cancel safely and terminal orders reject cancellation.
- Acceptance criteria: AC-012, AC-016.
- Tests required: API and integration tests.
- Definition of done: Cancellation behavior documented and tested.

### ENG-010: Implement Notification Worker

- Objective: Process customer notification requests asynchronously.
- Description: Emit `NotificationRequested`, process it with worker or Lambda, record attempts, and handle failure retries.
- Dependencies: ENG-007, ENG-008, notification runtime decision.
- Affected components: Order service, notification worker, queue/DLQ.
- Expected behavior: Order outcomes produce notification attempts.
- Acceptance criteria: AC-022 through AC-024.
- Tests required: Worker and queue integration tests.
- Definition of done: Notification workflow succeeds and failure path reaches DLQ.

### ENG-011: Add Terraform Infrastructure

- Objective: Provision AWS resources.
- Description: Create Terraform modules for IAM, SQS/SNS, RDS/MySQL, Lambda workers, and application deployment.
- Dependencies: ENG-007, messaging topology decision, deployment target decision.
- Affected components: Terraform, AWS.
- Expected behavior: Infrastructure can be planned and applied from clean configuration.
- Acceptance criteria: AC-025, AC-026.
- Tests required: `terraform fmt`, `terraform validate`, plan check.
- Definition of done: Terraform modules documented with example variables.

### ENG-012: Add Observability and Health Checks

- Objective: Make the system diagnosable.
- Description: Add Actuator health and metrics, structured logging, CloudWatch metric definitions, and alarm resources.
- Dependencies: ENG-003, ENG-008, ENG-011.
- Affected components: All services, Terraform, CloudWatch.
- Expected behavior: Operators can inspect health, metrics, logs, and queue state.
- Acceptance criteria: AC-027 through AC-029.
- Tests required: Smoke tests and log assertion tests.
- Definition of done: Observability checklist satisfied.

### ENG-013: Complete Documentation and Runbook

- Objective: Prepare project for reviewer and developer use.
- Description: Update README with architecture, local setup, AWS deployment, teardown, troubleshooting, and roadmap.
- Dependencies: ENG-001 through ENG-012 as applicable.
- Affected components: Documentation.
- Expected behavior: A new developer can run and understand the system.
- Acceptance criteria: AC-030.
- Tests required: Documentation walkthrough.
- Definition of done: README and docs match implemented behavior.

## Quality Gate

- Every important PRD requirement is represented in engineering artifacts.
- Testable behavior exists for each requirement or the ambiguity is explicitly flagged.
- Dependencies are identified by task ID or external decision.
- Non-functional requirements for reliability, performance, maintainability, security, and observability are captured.
- Security and observability requirements are represented in dedicated documents.
- Implementation tasks include dependencies, tests, acceptance criteria, and definition of done.
