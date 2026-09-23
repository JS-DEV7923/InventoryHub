# InventoryHub Performance Results

## Performance Requirements

- Catalog lookup APIs should respond within 300 ms locally for typical test data.
- Order creation should respond within 500 ms after persistence and event publication.
- 95% of asynchronous inventory decisions should complete within 10 seconds under normal load.

## Results

No performance benchmark was executed in this validation pass.

The existing `mvn test` suite verifies functional behavior but does not collect API latency, throughput, resource usage, async decision latency, or concurrency behavior.

## Confidence

Low for performance compliance. The implementation is small and likely fast for local H2 test data, but the documented budgets are not verified.

## Required Follow-Up

- Add lightweight API timing tests for catalog and order creation.
- Add an async decision latency test around order creation to final order status.
- Add a reservation concurrency test to prove inventory does not go negative.
- Run a MySQL-backed smoke test because H2 lock behavior may differ from MySQL.
