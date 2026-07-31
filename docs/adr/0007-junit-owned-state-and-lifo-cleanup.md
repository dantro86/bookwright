# ADR 0007: JUnit-owned state and LIFO cleanup

- Status: Accepted
- Date: 2026-07-31

## Context

Parallel tests need isolated fixtures, authentication data, teardown actions, and browser resources. Static fields and `ThreadLocal` storage couple state to worker threads rather than to the test lifecycle.

## Decision

Store method state in `ExtensionContext.Store` under explicit namespaces. Register class/run resources as `CloseableResource` values. Record cleanup actions in the method store and execute them last-in-first-out because dependent resources must be destroyed in the reverse order of creation.

Cleanup failures follow `teardown.failOnError`: they may fail a successful test, but they never replace the primary test failure.

## Consequences

- JUnit owns state lifetime and invokes cleanup even when execution fails.
- Parallel scheduling cannot move state between tests.
- Tests may create several dependent resources without hard-coding teardown order.
- Extensions require focused lifecycle self-tests because callback order is part of the framework contract.
