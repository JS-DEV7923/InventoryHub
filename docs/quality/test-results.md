# InventoryHub Test Results

## Automated Test Command

```bash
mvn test
```

Outcome:

- Build success.
- Tests run: 5.
- Failures: 0.
- Errors: 0.
- Skipped: 0.

Covered automated cases:

- Active product listing.
- Order creation with correlation ID and idempotency key.
- Successful inventory reservation and order confirmation.
- Insufficient stock cancellation with unchanged inventory.
- Inactive product rejection.
- Repeated idempotency key returns existing order.

## Infrastructure Command

```bash
terraform version
```

Outcome:

- Failed locally with `zsh:1: command not found: terraform`.
- Terraform formatting, validation, and plan were not executed.

## Static Checks

Secret scan command:

```bash
rg -n "password|secret|api[_-]?key|access[_-]?key|BEGIN (RSA|OPENSSH|PRIVATE)|AKIA|token" -S -g '!.git' -g '!target'
```

Outcome:

- No real committed credentials detected.
- Hits were placeholders, variable names, or documentation references.

## Regression Observations

- The test suite validates the local in-process event flow, not AWS SNS/SQS behavior.
- The test suite does not validate Terraform resources, MySQL runtime behavior, concurrency, DLQ behavior, or performance budgets.
- The automated tests are useful for MVP confidence but are not sufficient for production release against the full specs.
