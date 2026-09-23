# InventoryHub Security Requirements

Source: [InventoryHub PRD](../PRD.md)

## Authentication and Authorization

- Authentication is not required for the local MVP unless the project is deployed publicly.
- If deployed publicly, add a lightweight API key or gateway-level protection before exposing write APIs.
- Administrative endpoints must not be publicly reachable in deployed environments.
- Actuator details should be restricted or sanitized outside local development.

## Input Validation

- Validate all external API payloads.
- Reject invalid email addresses, missing products, inactive products, empty line items, non-positive quantities, and malformed IDs.
- Reject invalid event envelope fields before business processing.
- Return safe validation messages without internal stack traces.

## Data Protection

- Treat customer email as personal data.
- Do not log full request payloads when they include customer contact details.
- Avoid storing unnecessary customer data.
- Use TLS for deployed API and database connections where supported by the chosen deployment target.

## Secrets Management

- Database credentials, AWS credentials, queue URLs, and API keys must not be committed.
- Use environment variables, Terraform variables, or managed secret stores.
- Provide `.env.example` style documentation without real secrets when implementation begins.

## IAM and Cloud Permissions

- Terraform-managed roles must follow least privilege.
- Services should receive access only to queues, topics, logs, and secrets they need.
- Lambda workers should not receive broad AWS account permissions.

## Auditability

- Order state transitions must be persisted with timestamps.
- Event processing records must capture event ID, consumer name, status, processed time, and failure summary.
- Logs must include correlation IDs for incident review.

## Dependency Trust

- Pin dependency versions through build tooling.
- Use dependency scanning in CI when available.
- Do not add unreviewed libraries for core reservation, messaging, or security behavior.

## Abuse and Failure Prevention

- Rate limiting is not specified in the PRD and is not required for local MVP.
- Public deployment should include rate limiting or gateway throttling before exposing order creation.
- Idempotency keys should be used to reduce accidental duplicate orders.
