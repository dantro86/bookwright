# ADR 0004: Explicit retries at the consistency boundary

- Status: Accepted
- Date: 2026-07-31

## Context

Retries hidden in an HTTP client can turn one test action into several requests, duplicate writes, hide service
instability, and make timing diagnostics misleading. Retrying every exception in a polling utility is equally
dangerous because assertion failures and programming errors may be mistaken for transient infrastructure states.

Some operations still require polling: a service may be warming up, or a newly created entity may become visible
through an eventually consistent read model after the write succeeds.

## Decision

The shared OkHttp client sets `retryOnConnectionFailure(false)`. `Calls` executes each Retrofit `Call` exactly
once and distinguishes transport failures (`ApiCallException`) from response-contract failures
(`UnexpectedResponseException`).

When a specific workflow is known to be eventually consistent, its step uses Awaitility at that call site. The
wait retries only `ApiCallException`; unexpected status codes, malformed expectations, assertion failures, and
programming errors fail immediately.

MockWebServer tests prove both sides of the contract: a disconnected request is not retried globally, while an
explicit wait can retry a transient transport failure and stops after the first response-contract failure.

## Consequences

- Every retry is visible in the business step and has a named timeout diagnostic.
- Non-idempotent writes are never repeated by shared infrastructure.
- Temporary transport failures outside an explicit consistency boundary fail the test immediately.
- Teams must decide deliberately which reads are eventually consistent instead of applying a global policy.
