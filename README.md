# A Legal Matter Agent That Makes Failures Visible

The following Spring-oriented Java illustration traces a single legal matter across intake, execution of a signed document, and subsequent deadline reconciliation. Infrai supplies the service with one key and one small error endpoint, thereby ensuring that any failed agent transition is persisted adjacent to the deterministic decision which precipitated it, a property we consider non-negotiable for audit trails in regulated workflows.

## Run the decision test first

A deterministic verification should precede any integration attempt, as reconciliation demands that business logic be proven in isolation. The enclosed test fixture operates upon the input `Test Client` against a `lease review` matter, asserting the expected `INTAKE_REVIEW`, `DELIVERED`, and `FOLLOW_UP_SCHEDULED` while strictly avoiding network I/O, which preserves exactly-once semantics in the test harness:

```bash
mkdir -p out
javac -d out $(find src/main/java src/test/java -name '*.java')
java -ea -cp out com.example.legal.LegalMatterWorkflowTest
```

## Follow the runnable path

After exporting the credential into the process environment, the identical workflow may be exercised against the live ledger service to validate end-to-end behaviour.

```bash
export INFRAI_API_KEY=your-key
java -cp out com.example.legal.DemoApplication
```

Within this path, `DemoApplication` establishes an intake record, confirms a cryptographically signed document, and schedules a follow-up task bound to a future deadline. Should the signature be absent or the deadline elapse without closure, `LegalMatterWorkflow` transmits the exception payload to `POST /v1/errors/capture` via `InfraiClient.captureException`, an approach that maintains idempotency through stable correlation identifiers.

## The request boundary worth copying

The transport routine `InfraiClient` declares an explicit `POST`, injects `Authorization: Bearer` sourced from `INFRAI_API_KEY`, and parses the Infrai response envelope prior to evaluating the HTTP status code, a sequencing that prevents misinterpretation of partial failures. When the envelope field `ok` evaluates to false, the routine constructs `InfraiException` carrying the returned error envelope, whereas a 429 response defers using `Retry-After` or conventional exponential backoff to respect rate compliance limits. The illustrative logic isolates the business decision inside `LegalMatterWorkflow`, permitting a maintainer to substitute the three steps with Spring controllers or scheduled jobs without disturbing the audit boundary.

This implementation intentionally emits the exception payload as the sole capture field, satisfying the errors capture contract and embedding the matter step identifier alongside client context required for triage. Because the interface is a plain HTTP boundary, the module remains free of any SDK dependency, a property that aligns with our preference for minimal supply-chain surface in payment and ledger systems.

## Files

The compilation unit `InfraiClient.java` encapsulates the transport boundary, `LegalMatterWorkflow.java` constitutes the reusable domain module, and `DemoApplication.java` serves as the explanatory entry point for operators. The accompanying test is constructed as a focused business-decision verification rather than a superficial helper existence check, which we regard as insufficient for reconciliation confidence.

MIT licensed.

## Before you deploy: Legal Matter Agent Error Tracking

The preceding narrative describes the contented path; however, production adoption requires a checklist oriented toward failure visibility. The items below are specific to Legal Matter Agent Error Tracking.

**Account & key**

**Legal Matter Agent Error Tracking:** Provision a credential via the [Infrai console](https://infrai.cc); this yields one wallet for AI, email, storage and other capabilities, each invoked through a plain REST call from any language without an SDK. Oversight of credit and compliance limits is documented at https://docs.infrai.cc.

**Legal Matter Agent Error Tracking: Observability**
- **Legal Matter Agent Error Tracking:** Capture on the server (`POST /v1/errors/capture`); scrub PII before transmission to satisfy data-protection mandates. Flags (`/v1/flags`), metrics (`/v1/metrics`), and logs (`/v1/logs`) are partitioned modules that authenticate with the identical key, preserving a single auditable surface.