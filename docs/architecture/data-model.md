# InventoryHub Data Model

## Storage Choice

MySQL is the system of record for product metadata, orders, inventory, reservations, event outbox rows, event processing records, and notification attempts. In cloud deployments, MySQL should run on RDS.

## Ownership

| Entity | Owner |
| --- | --- |
| Product | Catalog Service |
| Order | Order Service |
| Order Line | Order Service |
| Inventory Item | Inventory Service |
| Reservation | Inventory Service |
| Event Outbox | Publishing service |
| Event Processing Record | Consuming service |
| Notification Attempt | Notification Worker |

## Entities

### `products`

| Column | Type | Notes |
| --- | --- | --- |
| `id` | UUID or BIGINT | Primary key |
| `sku` | VARCHAR | Unique |
| `name` | VARCHAR | Required |
| `description` | TEXT | Optional |
| `price` | DECIMAL(10,2) | Fixed precision |
| `active` | BOOLEAN | Inactive products are not orderable |
| `created_at` | TIMESTAMP | Required |
| `updated_at` | TIMESTAMP | Required |

Indexes:

- Unique index on `sku`.
- Index on `active`.

### `orders`

| Column | Type | Notes |
| --- | --- | --- |
| `id` | UUID or BIGINT | Primary key |
| `customer_email` | VARCHAR | Personal data |
| `status` | VARCHAR | `PENDING_RESERVATION`, `CONFIRMED`, `CANCELLED` |
| `total_amount` | DECIMAL(10,2) | Fixed precision |
| `failure_reason` | VARCHAR | Nullable |
| `correlation_id` | VARCHAR | Required |
| `idempotency_key` | VARCHAR | Nullable, unique when present |
| `created_at` | TIMESTAMP | Required |
| `updated_at` | TIMESTAMP | Required |

Indexes:

- Index on `status`.
- Unique index on `idempotency_key` when present.
- Index on `correlation_id`.

### `order_lines`

| Column | Type | Notes |
| --- | --- | --- |
| `id` | UUID or BIGINT | Primary key |
| `order_id` | FK | References `orders.id` |
| `product_id` | UUID or BIGINT | Product identifier |
| `quantity` | INT | Must be positive |
| `unit_price` | DECIMAL(10,2) | Price captured at order time |

Indexes:

- Index on `order_id`.
- Index on `product_id`.

### `inventory_items`

| Column | Type | Notes |
| --- | --- | --- |
| `id` | UUID or BIGINT | Primary key |
| `product_id` | UUID or BIGINT | Unique product identifier |
| `available_quantity` | INT | Must be non-negative |
| `reserved_quantity` | INT | Must be non-negative |
| `version` | INT | Optimistic locking |
| `updated_at` | TIMESTAMP | Required |

Indexes:

- Unique index on `product_id`.

Concurrency:

- Use optimistic locking with `version`, or conditional updates such as `available_quantity >= requested_quantity`.
- Reservation and quantity updates must occur in the same transaction.

### `reservations`

| Column | Type | Notes |
| --- | --- | --- |
| `id` | UUID or BIGINT | Primary key |
| `order_id` | UUID or BIGINT | Required |
| `product_id` | UUID or BIGINT | Required |
| `quantity` | INT | Required |
| `status` | VARCHAR | `PENDING`, `RESERVED`, `FAILED`, `RELEASED`, `CONFIRMED` |
| `created_at` | TIMESTAMP | Required |
| `updated_at` | TIMESTAMP | Required |

Indexes:

- Index on `order_id`.
- Index on `product_id`.
- Composite index on `order_id`, `product_id`.

### `event_outbox`

| Column | Type | Notes |
| --- | --- | --- |
| `id` | UUID or BIGINT | Primary key |
| `event_id` | UUID | Unique |
| `event_type` | VARCHAR | Required |
| `aggregate_id` | VARCHAR | Required |
| `correlation_id` | VARCHAR | Required |
| `payload` | JSON | Required |
| `status` | VARCHAR | `PENDING`, `PUBLISHED`, `FAILED` |
| `publish_attempts` | INT | Required |
| `last_error` | TEXT | Nullable |
| `created_at` | TIMESTAMP | Required |
| `published_at` | TIMESTAMP | Nullable |

Indexes:

- Unique index on `event_id`.
- Index on `status`, `created_at`.

### `event_processing_records`

| Column | Type | Notes |
| --- | --- | --- |
| `event_id` | UUID | Part of primary key |
| `consumer_name` | VARCHAR | Part of primary key |
| `status` | VARCHAR | `PROCESSED`, `FAILED` |
| `processed_at` | TIMESTAMP | Nullable |
| `error_message` | TEXT | Nullable |

Indexes:

- Primary key on `event_id`, `consumer_name`.

### `notification_attempts`

| Column | Type | Notes |
| --- | --- | --- |
| `id` | UUID or BIGINT | Primary key |
| `event_id` | UUID | Notification request event ID |
| `order_id` | UUID or BIGINT | Required |
| `customer_email` | VARCHAR | Personal data |
| `template` | VARCHAR | Required |
| `status` | VARCHAR | `SENT`, `FAILED`, `SIMULATED` |
| `error_message` | TEXT | Nullable |
| `created_at` | TIMESTAMP | Required |

Indexes:

- Index on `order_id`.
- Unique index on `event_id`.

## Migrations

- Use repeatable migration tooling such as Flyway or Liquibase.
- Migrations must be ordered, committed, and runnable from a clean database.
- Seed data may be included for local development only.

## Retention

- Orders and inventory records are retained indefinitely for MVP.
- Event outbox and processing records may be retained indefinitely for portfolio/demo clarity.
- Notification attempts may be retained indefinitely for MVP; future production use should define retention for customer contact data.
