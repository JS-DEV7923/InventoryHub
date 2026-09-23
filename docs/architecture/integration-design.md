# InventoryHub Integration Design

## Internal Integrations

### Order Service to Catalog Service

- Protocol: REST or in-process module interface for local modular implementation.
- Purpose: Validate product existence, active status, and price.
- Timeout: 500 ms default for REST calls.
- Retry: No automatic retry for validation requests in the synchronous order path unless the failure is known transient.
- Fallback: Return a safe `503` or `500` error if product validation cannot be completed.

### Order Service to SNS

- Protocol: AWS SDK publish.
- Purpose: Publish `OrderCreated`, order outcome, and notification request events.
- Reliability: Use transactional outbox so persisted order state is not separated from required event publication.
- Retry: Publisher retries pending outbox rows with bounded attempts and logs failures.

### SNS to SQS

- Protocol: SNS subscriptions to SQS queues.
- Purpose: Durable fan-out to inventory, order outcome, and notification consumers.
- Retry: SNS retries delivery to SQS according to AWS behavior; consumers own SQS retry/DLQ behavior.

### SQS to Inventory Service

- Protocol: SQS polling consumer.
- Purpose: Process `OrderCreated` and reservation release events.
- Retry: Failed processing leaves message unacknowledged for retry.
- DLQ: Required.

### SQS to Order Service

- Protocol: SQS polling consumer.
- Purpose: Process inventory outcome events.
- Retry: Failed processing leaves message unacknowledged for retry.
- DLQ: Required.

### SQS to Notification Lambda

- Protocol: Lambda event source mapping from SQS.
- Purpose: Process `NotificationRequested` events.
- Retry: Lambda/SQS redrive policy.
- DLQ: Required.

## AWS Resource Design

| Resource | Notes |
| --- | --- |
| `inventoryhub-lifecycle-events` SNS topic | Publishes order and inventory lifecycle events |
| `inventoryhub-inventory-reservation-queue` | Receives `OrderCreated` events |
| `inventoryhub-order-outcome-queue` | Receives inventory result events |
| `inventoryhub-notification-queue` | Receives `NotificationRequested` events |
| Matching DLQs | One DLQ per queue |
| RDS MySQL instance | Demo-sized, private where possible |
| IAM roles | Service-specific queue/topic access |
| CloudWatch log groups | One per service/worker |

## Local Testing

- MySQL runs through Docker Compose or a local instance.
- Localstack is recommended for SNS/SQS contract and DLQ tests.
- Notification delivery is simulated.
- Terraform validation can run without applying cloud resources.

## Credentials and Secrets

- AWS credentials are never stored in the repository.
- Queue URLs, topic ARNs, database URLs, and credentials are injected through environment variables or managed secrets.
- Terraform variables should support example placeholder values only.

## Fallback Behavior

- If Catalog Service cannot be reached, order creation fails safely before persistence.
- If event publication fails after persistence, outbox rows remain pending for retry.
- If a consumer fails, SQS retry and DLQ behavior handles recovery.
- If notification processing fails, order state remains confirmed or cancelled; notification failure is tracked separately.
