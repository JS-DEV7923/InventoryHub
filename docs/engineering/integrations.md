# InventoryHub Integrations

Source: [InventoryHub PRD](../PRD.md)

## Internal Service Integrations

| Integration | Producer | Consumer | Purpose |
| --- | --- | --- | --- |
| Product lookup | Order service | Catalog service or catalog module | Validate products and price line items |
| Order-created event | Order service | Inventory service | Start reservation workflow |
| Inventory outcome event | Inventory service | Order service | Confirm or cancel order |
| Notification request event | Order service | Notification worker | Send or simulate customer notification |

## AWS Integrations

| AWS Service | Usage | Required Configuration |
| --- | --- | --- |
| SQS | Durable event queues and DLQs | Queues, redrive policies, visibility timeouts, permissions |
| SNS | Event fan-out when selected | Topics, subscriptions, publishing permissions |
| RDS/MySQL | Managed relational persistence | Subnet/security configuration, credentials, backups for non-demo environments |
| Lambda | Notification or operational worker | IAM role, environment variables, event source mapping |
| IAM | Least-privilege access control | Service and worker roles, queue/topic/database permissions |
| CloudWatch | Logs, metrics, alarms | Log groups, metric alarms, dashboards where applicable |

## Local Development Integrations

- MySQL via Docker Compose or local instance.
- Optional Localstack for SQS/SNS integration testing.
- Spring Boot Actuator for local health and metrics.

## External Provider Integrations

No real email, SMS, payment gateway, or authentication provider is required for MVP. Notification delivery may be simulated until a provider is explicitly added.

## Integration Contracts

- All events must use the shared event envelope.
- All service-to-service calls must propagate correlation IDs.
- Queue messages must include enough metadata to support replay and debugging.
- Integration tests should verify both happy path and failure path behavior.

## Open Integration Decisions

| ID | Decision | Impact |
| --- | --- | --- |
| INT-001 | SNS fan-out versus direct SQS for specific events | Queue/topic Terraform topology and consumer wiring |
| INT-002 | Localstack usage for integration tests | CI complexity and test realism |
| INT-003 | Notification simulation mechanism | Worker code and test fixtures |
