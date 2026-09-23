# InventoryHub

InventoryHub is an order and inventory backend implemented with Java, Spring Boot, REST APIs, MySQL-ready persistence, event contracts, idempotent consumers, structured errors, health checks, and Terraform infrastructure scaffolding.

The implementation follows the documents in `docs/`:

- Product requirements: `docs/PRD.md`
- Engineering specs: `docs/engineering/`
- Architecture: `docs/architecture/`

## Current Scope

Implemented:

- Catalog APIs for active product listing and product detail lookup.
- Order APIs for create, lookup, and cancellation.
- Inventory APIs for stock lookup, reservation, and release.
- Order lifecycle flow: `OrderCreated` -> inventory reservation -> order confirmed or cancelled -> notification request.
- Event envelope, outbox records, and event processing records.
- Simulated notification attempts.
- Correlation IDs, structured API errors, validation, Actuator health and metrics.
- Integration tests for main order lifecycle behavior.
- Terraform module scaffolding for IAM, SQS/SNS, RDS/MySQL, Lambda, and application deployment metadata.

The local MVP uses an in-process event publisher to make the lifecycle testable without AWS. The architecture keeps the event envelope and outbox/idempotency tables aligned with the planned SNS/SQS deployment.

## Requirements

- Java 21 or newer.
- Maven 3.9 or newer.
- Docker, optional for MySQL.
- Terraform, optional for infrastructure planning.

## Run Locally With H2

```bash
mvn spring-boot:run
```

The default profile uses an in-memory H2 database and seed data from `src/main/resources/data.sql`.

Health:

```bash
curl http://localhost:8080/actuator/health
```

List products:

```bash
curl http://localhost:8080/api/products
```

Create an order:

```bash
curl -X POST http://localhost:8080/api/orders \
  -H 'Content-Type: application/json' \
  -H 'Idempotency-Key: demo-order-1' \
  -d '{
    "customerEmail": "customer@example.com",
    "items": [
      {
        "productId": "11111111-1111-1111-1111-111111111111",
        "quantity": 2
      }
    ]
  }'
```

## Run With MySQL

Start MySQL:

```bash
docker compose up -d mysql
```

Docker Compose reads local MySQL values from an ignored `.env` file. Use `.env.example` as the placeholder reference and keep real local passwords out of Git.

Run the app against MySQL:

```bash
DB_URL=jdbc:mysql://localhost:3306/inventoryhub \
DB_USERNAME=inventoryhub \
DB_PASSWORD=replace-with-local-password \
DB_DRIVER=com.mysql.cj.jdbc.Driver \
SQL_INIT_MODE=never \
mvn spring-boot:run
```

For real environments, use managed secrets and do not reuse the local development password.

## Tests

```bash
mvn test
```

The current test suite covers:

- Catalog listing.
- Successful order creation and confirmation.
- Insufficient-stock cancellation.
- Inactive product rejection.
- Idempotency key reuse.

## Key API Endpoints

- `GET /api/products`
- `GET /api/products/{productId}`
- `POST /api/orders`
- `GET /api/orders/{orderId}`
- `POST /api/orders/{orderId}/cancel`
- `GET /api/inventory/{productId}`
- `POST /api/inventory/reservations`
- `POST /api/inventory/reservations/{reservationId}/release`
- `GET /actuator/health`
- `GET /actuator/metrics`

## Terraform

Terraform files live in `infra/terraform`.

Example commands:

```bash
cd infra/terraform
terraform init
terraform fmt -recursive
terraform validate
terraform plan -var-file=example.tfvars.example
```

The Terraform package is a scaffold for the approved architecture. Review cost, networking, credentials, state backend, and deployment target choices before applying in an AWS account.

## Roadmap

1. Replace in-process event delivery with AWS SNS/SQS adapters.
2. Add database migrations with Flyway or Liquibase.
3. Add a real Lambda package for notification processing.
4. Add CloudWatch dashboards and alarms from the observability design.
5. Choose and implement the Spring Boot deployment target.
6. Add API protection before exposing write endpoints publicly.

## Teardown

Local MySQL:

```bash
docker compose down -v
```

Terraform-managed cloud resources:

```bash
cd infra/terraform
terraform destroy -var-file=example.tfvars.example
```
