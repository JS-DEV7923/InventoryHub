# InventoryHub Security Test Results

## Security-Sensitive Requirements

- Externalize secrets and sensitive configuration.
- Validate external API payloads.
- Avoid exposing internal exception details.
- Restrict public write APIs before deployment.
- Use least-privilege IAM.
- Preserve auditability through order transitions and event processing records.

## Evidence

| Check | Evidence | Result |
| --- | --- | --- |
| Secret scan | `rg` scan for credential-like patterns | PASS |
| API validation | Bean Validation annotations and global exception handler | PARTIAL |
| Internal exception exposure | `GlobalExceptionHandler` maps unexpected failures to safe 500 body | PASS |
| Public authentication | Security docs say required before public deployment; no auth implemented | NOT TESTED / NOT IMPLEMENTED |
| IAM least privilege | Terraform IAM policies scoped to SNS/SQS/log actions | PARTIAL |
| Auditability | Order timestamps, outbox, event processing records, notification attempts | PARTIAL |

## Findings

- No real secrets were found in committed files.
- `.env.example` and Terraform example values use placeholders.
- Authentication is intentionally absent for local MVP. This blocks public deployment until API protection is added.
- Validation coverage is incomplete. Unknown product during order creation likely returns `404`, but the API contract and AC-007 require `400`.
- IAM was reviewed statically only; no Terraform validation or cloud deployment was exercised.

## Required Follow-Up

- Add security tests for malformed payloads, unknown products, non-positive quantities, empty items, and internal error responses.
- Add API key or gateway authentication before public deployment.
- Run Terraform validation and AWS IAM policy review in an environment with Terraform installed.
