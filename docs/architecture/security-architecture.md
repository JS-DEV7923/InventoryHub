# InventoryHub Security Architecture

## Trust Boundaries

| Boundary | Protection |
| --- | --- |
| Client to API | Future API key or gateway auth for deployed write APIs |
| API to database | Private network where possible, TLS where supported, credential injection |
| Services to SNS/SQS | IAM roles with least privilege |
| SQS to Lambda | Event source mapping and Lambda execution role |
| Operator to CloudWatch | AWS IAM outside application scope |

## Authentication and Authorization

- Local MVP does not require authentication.
- Public deployment must protect write APIs with API key, gateway authorizer, or equivalent control before exposure.
- Actuator endpoints must be restricted or sanitized outside local development.
- No admin UI or privileged user roles are included in MVP.

## Input and Event Validation

- Validate all REST payloads at controller boundaries.
- Validate all event envelopes before business processing.
- Reject malformed IDs, invalid quantities, invalid status values, and unknown event types.
- Never use unvalidated message payloads for state transitions.

## Data Protection

- Customer email is personal data.
- Avoid logging full request bodies and notification payloads.
- Mask or omit customer email from error logs unless needed for debugging in controlled environments.
- Use fixed precision for money values to prevent calculation surprises.

## Secrets

- Store database credentials and AWS credentials outside source control.
- Prefer AWS Secrets Manager or SSM Parameter Store for deployed environments.
- Local examples may use `.env.example` with placeholder values only.
- Terraform variables must not include real secrets in committed files.

## IAM

- Order Service role can publish lifecycle events and consume order outcome events.
- Inventory Service role can consume reservation events and publish inventory outcome events.
- Notification Lambda role can consume notification queue messages, write logs, and access only required database credentials.
- Terraform execution role needs provisioning privileges, but runtime roles must remain narrow.

## Auditability

- Persist order status transitions with timestamps.
- Persist event processing records by event ID and consumer name.
- Persist notification attempts.
- Preserve correlation IDs across API logs and event logs.

## Threat Considerations

| Threat | Mitigation |
| --- | --- |
| Duplicate orders from client retries | Idempotency keys on order creation |
| Public API abuse | Require API protection before public deployment |
| Oversharing sensitive logs | Structured logging rules and payload redaction |
| Over-broad cloud permissions | Least-privilege IAM modules |
| Malformed event injection | Envelope validation and queue IAM restrictions |
| Internal exception leakage | Safe error responses |
