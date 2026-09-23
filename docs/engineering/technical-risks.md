# InventoryHub Technical Risks

Source: [InventoryHub PRD](../PRD.md)

| Risk ID | Risk | Impact | Likelihood | Blocking | Mitigation |
| --- | --- | --- | --- | --- | --- |
| RISK-001 | Inventory overselling under concurrent orders | Confirmed orders may exceed stock | Medium | Yes for release | Use conditional updates or optimistic locking; add concurrency tests |
| RISK-002 | Duplicate SQS deliveries causing repeated state transitions | Incorrect order or notification state | High | Yes for async milestone | Store event processing records keyed by event ID and consumer name |
| RISK-003 | Transaction succeeds but event publish fails | Order remains pending without reservation workflow | Medium | Yes for robust release | Use transactional outbox or clear retryable publish mechanism |
| RISK-004 | Out-of-order events | Invalid state transitions | Medium | No for MVP if handled conservatively | Enforce state machine transitions and log rejected stale events |
| RISK-005 | DLQ messages not monitored | Failed workflows remain unnoticed | Medium | No for local MVP | Add DLQ metrics and alarms in production readiness milestone |
| RISK-006 | Terraform cloud resources cause unexpected cost | User incurs AWS spend | Medium | No | Use small defaults, document destroy flow, add cost notes |
| RISK-007 | Deployment target unspecified | Terraform app module may be delayed | High | Blocks Milestone 6 | Decide ECS, Elastic Beanstalk, Lambda, or simple host before cloud deployment |
| RISK-008 | Authentication omitted in public environment | Public write APIs can be abused | Medium | Blocks public deployment | Add API key or gateway auth before public exposure |
| RISK-009 | Notification runtime unclear | Worker implementation may be reworked | Medium | No | Choose Lambda or Spring Boot worker before notification milestone |
| RISK-010 | Local AWS service emulation differs from AWS behavior | Integration tests may miss cloud issues | Medium | No | Use Localstack for fast tests and smoke-test real AWS before release |

## Blocking Risks

- RISK-001 blocks order workflow release.
- RISK-002 blocks asynchronous messaging release.
- RISK-003 should be addressed before presenting the system as production-aware.
- RISK-007 blocks final AWS deployment implementation.
- RISK-008 blocks any public deployment.

## Non-Blocking Ambiguities

The PRD open questions should be resolved as implementation progresses, but they do not block initial domain modeling, API contracts, database schemas, local development, or tests.
