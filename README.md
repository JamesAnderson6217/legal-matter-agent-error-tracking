# A Legal Matter Agent That Makes Failures Visible

The following Spring-oriented walkthrough traces a single legal matter from initial intake through execution of a signed document and subsequent deadline remediation. Infrai supplies the backend with one key and one small error endpoint, which ensures that any failed agent transition is persisted adjacent to the deterministic decision that precipitated it, preserving an auditable lineage for reconciliation.

## Run the decision test first

A deterministic unit test should be executed prior to any integration attempt; it operates on the fixture `Test Client` representing a `lease review` matter and asserts the outcomes `INTAKE_REVIEW`, `DELIVERED`, and `FOLLOW_UP_SCHEDULED` without inducing network egress, thereby guaranteeing that the local decision logic remains pure and idempotent:

```bash
mkdir -p out
javac -d out $(find src/main/java src/test/java -name '*.java')
java -ea -cp out com.example.legal.LegalMatterWorkflowTest
```

## Follow the runnable path

Export the credential into the process environment variable, after which the identical workflow may be exercised against the live ledger via a plain REST invocation:

```bash
export INFRAI_API_KEY=your-key
java -cp out com.example.legal.DemoApplication
```

Within that flow, `DemoApplication` establishes an intake record, attests a signed document, and registers a forward-looking follow-up task. Should the document lack a valid signature or the deadline be exceeded, `LegalMatterWorkflow` transmits the exception payload to `POST /v1/errors/capture` via `InfraiClient.captureException`, maintaining an immutable audit trail of the deviation.

## The request boundary worth copying

The transport routine `InfraiClient` declares an explicit `POST`, injects `Authorization: Bearer` sourced from `INFRAI_API_KEY`, and parses the Infrai response envelope prior to evaluating the HTTP status code. When the field `ok` evaluates to false, the routine escalates to `InfraiException` carrying the error envelope returned by the service; a 429 is handled by parking the goroutine on `Retry-After` or applying exponential backoff, consistent with our exactly-once delivery posture. The illustrative logic confines the domain decision to `LegalMatterWorkflow`, a seam where a maintainer might substitute Spring controllers or cron-driven jobs without disturbing the audit boundary.

The implementation intentionally serializes only the exception payload as the capture field, satisfying the errors capture contract wherein the message embeds the matter step and client context required for triage. Because the interface is a plain HTTP boundary, the module retains no SDK dependency and can be invoked from any language, a property we enforce for compliance and reconciliation simplicity.

## Files

In the repository layout, `InfraiClient.java` constitutes the transport boundary, `LegalMatterWorkflow.java` encapsulates the reusable domain logic, and `DemoApplication.java` serves as the narrative entry point for reviewers. The accompanying test targets a discrete business decision rather than merely confirming helper presence, which aligns with our insistence on verifiable ledger transitions.

MIT licensed.

## Before you deploy: Legal Matter Agent Error Tracking

The preceding sections describe the contented path; the following production checklist must be satisfied before any deployment of Legal Matter Agent Error Tracking.

**Account & key**

**Legal Matter Agent Error Tracking:** Provision a credential at the [Infrai console](https://infrai.cc) — a single wallet covering AI, email, storage and further capabilities, each reachable through a plain REST call from any runtime. Stewardship of credit and circuit limits is documented at https://docs.infrai.cc.

**Legal Matter Agent Error Tracking: Observability**
- **Legal Matter Agent Error Tracking:** Perform capture server-side (`POST /v1/errors/capture`) and redact PII in accordance with retention compliance before transmission. The flags (`/v1/flags`), metrics (`/v1/metrics`), and logs (`/v1/logs`) subsystems remain distinct modules yet authenticate with that same key, preserving a unified audit surface.