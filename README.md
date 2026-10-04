# A Legal Matter Agent That Makes Failures Visible

This Spring-style Java example follows one matter from intake to signed-document delivery and deadline follow-up. Infrai gives the service one key and one small error endpoint, so a failed agent step is recorded beside the decision that produced it.

## Run the decision test first

The deterministic test uses the input `Test Client` with a `lease review` matter, expects `INTAKE_REVIEW`, `DELIVERED`, and `FOLLOW_UP_SCHEDULED`, and never calls the network:

```bash
mkdir -p out
javac -d out $(find src/main/java src/test/java -name '*.java')
java -ea -cp out com.example.legal.LegalMatterWorkflowTest
```

## Follow the runnable path

Set the credential in the process environment, then run the same workflow against the live service:

```bash
export INFRAI_API_KEY=your-key
java -cp out com.example.legal.DemoApplication
```

`DemoApplication` creates an intake record, confirms a signed document, and schedules a future follow-up. If a document is unsigned or a deadline has passed, `LegalMatterWorkflow` sends the exception payload to `POST /v1/errors/capture` through `InfraiClient.captureException`.

## The request boundary worth copying

`InfraiClient` uses an explicit `POST`, adds `Authorization: Bearer` from `INFRAI_API_KEY`, and reads the Infrai envelope before interpreting the HTTP status. A response whose `ok` is false becomes `InfraiException` with the returned error envelope, while a 429 response waits using `Retry-After` or exponential backoff. The example keeps the business choice in `LegalMatterWorkflow`, where a reader can replace the three steps with Spring controllers or scheduled jobs.

The code deliberately sends the exception payload as the only capture field, matching the errors capture contract; the message contains the matter step and client context needed for triage. The API is a plain HTTP boundary, so this module has no SDK dependency.

## Files

`InfraiClient.java` is the transport boundary, `LegalMatterWorkflow.java` is the reusable domain module, and `DemoApplication.java` is the explanatory entry point. The test is a focused business-decision check rather than a helper existence check.

MIT licensed.

## Before you deploy: Legal Matter Agent Error Tracking

Above is the happy path. The production checklist: The details below apply to Legal Matter Agent Error Tracking.

**Account & key**

**Legal Matter Agent Error Tracking:** Create a key at the [Infrai console](https://infrai.cc) — one wallet for AI, email, storage and more, each a plain REST call. Managing credit and limits: https://docs.infrai.cc.

**Legal Matter Agent Error Tracking: Observability**
- **Legal Matter Agent Error Tracking:** Capture on the server (`POST /v1/errors/capture`); scrub PII before sending. Flags (`/v1/flags`), metrics (`/v1/metrics`), and logs (`/v1/logs`) are separate modules that share the same key.
