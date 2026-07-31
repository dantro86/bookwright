# ADR 0008: Native waits and explicit consistency boundaries

- Status: Accepted
- Date: 2026-07-31

## Context

Generic retry helpers can hide programming errors, multiply HTTP side effects, and make timeout failures difficult to diagnose. UI and API synchronization also have different semantics.

## Decision

Use Playwright's auto-waiting locators and assertions for UI state. Use Awaitility only at named asynchronous API or infrastructure boundaries. Awaitility may ignore known transient transport failures or a domain-specific "not ready yet" result; all other failures stop immediately.

The shared OkHttp client performs no global retries. See [ADR 0004](0004-explicit-retries.md) for transport policy.

## Consequences

- Every retry has a visible alias, timeout, and business reason.
- Non-idempotent requests execute once unless a scenario deliberately repeats them.
- Programming errors fail near their source.
- Polling lambdas call raw clients rather than `@Step` methods, preventing duplicated Allure steps.
