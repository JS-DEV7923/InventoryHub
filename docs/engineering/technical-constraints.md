# InventoryHub Technical Constraints

Source: [InventoryHub PRD](../PRD.md)

## Platform Constraints

- Backend language and framework: Java with Spring Boot.
- Persistence: MySQL-compatible relational database.
- Messaging: AWS SQS/SNS.
- Infrastructure as code: Terraform.
- Worker runtime: Lambda is required for at least notification or operational worker coverage unless a later decision narrows its scope.

## API Constraints

- REST APIs must use predictable resource-oriented paths.
- API error responses must be structured and safe to expose.
- Health and metrics endpoints must use Spring Boot Actuator-compatible paths.
- Order creation must persist before event publication is considered successful.

## Data Constraints

- Product metadata and inventory quantities are owned by different bounded contexts.
- Inventory reservations must be transactionally safe under concurrent requests.
- Money values must use fixed precision decimal types.
- Event processing records must support idempotency checks by event ID and consumer name.
- Secrets must not be stored in source control.

## Reliability Constraints

- Message consumers must assume at-least-once delivery.
- Retry policies must be bounded.
- Every async queue must have a DLQ or documented exception.
- Duplicate messages must not produce duplicate state changes.

## Performance Constraints

- Product lookup APIs should respond within 300 ms locally with typical test data.
- Order creation should respond within 500 ms after persistence and event publication.
- 95% of async inventory decisions should complete within 10 seconds in test environments.

## Deployment Constraints

- Terraform must support environment-specific values.
- Infrastructure must include IAM, queues/topics, RDS/MySQL, Lambda workers, and app deployment resources.
- Deployment target for Spring Boot services is not specified in the PRD and remains an open decision.

## Documentation Constraints

- README must include local setup, architecture, incremental roadmap, AWS deployment, and teardown instructions.
- API and event schemas must be documented before implementation is considered complete.
