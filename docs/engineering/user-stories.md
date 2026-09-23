# InventoryHub User Stories

Source: [InventoryHub PRD](../PRD.md)

## Epic E1: Product Catalog

### US-001: List Available Products

- Actor: API client
- Goal: View products that can be ordered
- Value: Enables customers or test clients to build an order
- Priority: P0
- Dependencies: Product schema and seed data
- Source: PRD sections 8, 10, 14

### US-002: View Product Detail

- Actor: API client
- Goal: Retrieve product details by product ID
- Value: Supports order composition and product validation
- Priority: P0
- Dependencies: Product schema
- Source: PRD sections 10, 14

## Epic E2: Order Lifecycle

### US-003: Create Pending Order

- Actor: API client
- Goal: Submit customer contact details and line items
- Value: Starts the order workflow reliably
- Priority: P0
- Dependencies: Catalog API, order schema
- Source: PRD sections 8, 10, 14

### US-004: Check Order Status

- Actor: API client or support user
- Goal: Retrieve current order status and details
- Value: Makes async order outcomes visible
- Priority: P0
- Dependencies: Order persistence
- Source: PRD sections 3, 10, 14

### US-005: Cancel Order

- Actor: API client or operator
- Goal: Cancel an eligible order
- Value: Releases inventory and stops further processing
- Priority: P1
- Dependencies: Reservation release support
- Source: PRD sections 10, 14

## Epic E3: Inventory Reservations

### US-006: Reserve Stock

- Actor: Inventory service consumer
- Goal: Reserve stock for pending order line items
- Value: Prevents overselling
- Priority: P0
- Dependencies: Inventory schema, order-created event
- Source: PRD sections 8, 9, 10, 16

### US-007: Reject Insufficient Stock

- Actor: Inventory service consumer
- Goal: Reject reservation requests when stock is unavailable
- Value: Keeps order state accurate and avoids negative inventory
- Priority: P0
- Dependencies: Inventory reservation workflow
- Source: PRD sections 10, 16

### US-008: Release Reservation

- Actor: Order service or operator
- Goal: Release reserved stock for cancelled or failed orders
- Value: Restores inventory availability
- Priority: P1
- Dependencies: Reservation records
- Source: PRD sections 10, 14

## Epic E4: Asynchronous Messaging

### US-009: Publish Order Events

- Actor: Order service
- Goal: Publish lifecycle events after state changes
- Value: Decouples inventory and notification work
- Priority: P0
- Dependencies: Event envelope, queue/topic infrastructure
- Source: PRD sections 10, 13

### US-010: Consume Events Idempotently

- Actor: Message consumer
- Goal: Ignore duplicate events already processed
- Value: Protects state from at-least-once delivery
- Priority: P0
- Dependencies: Event processing records
- Source: PRD sections 10, 11, 12

### US-011: Route Failed Events to DLQ

- Actor: Operator
- Goal: Inspect events that could not be processed
- Value: Enables recovery from failed async work
- Priority: P0
- Dependencies: SQS redrive policy and monitoring
- Source: PRD sections 8, 10, 11

## Epic E5: Notifications

### US-012: Request Notification for Order Outcome

- Actor: Order service
- Goal: Emit notification requests for confirmed or cancelled orders
- Value: Keeps customers informed asynchronously
- Priority: P1
- Dependencies: Order outcome events
- Source: PRD sections 8, 10, 13

### US-013: Process Notification Request

- Actor: Notification worker
- Goal: Send or simulate a customer notification and record the attempt
- Value: Demonstrates downstream worker reliability
- Priority: P1
- Dependencies: Notification queue, worker runtime
- Source: PRD sections 9, 10

## Epic E6: Infrastructure and Operations

### US-014: Provision Cloud Resources

- Actor: Developer
- Goal: Provision infrastructure with Terraform
- Value: Makes the system reproducible
- Priority: P1
- Dependencies: AWS account configuration
- Source: PRD sections 10, 16, 17

### US-015: Observe System Health

- Actor: Operator
- Goal: Check service health, logs, metrics, and queues
- Value: Makes failures diagnosable
- Priority: P1
- Dependencies: structured logging, metrics, CloudWatch
- Source: PRD sections 11, 20

### US-016: Run Locally

- Actor: Developer
- Goal: Start services and dependencies locally
- Value: Supports incremental development and tests
- Priority: P0
- Dependencies: local MySQL and service configuration
- Source: PRD sections 7, 17
