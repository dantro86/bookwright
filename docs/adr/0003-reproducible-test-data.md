# ADR 0003: Reproducible test data

- Status: Accepted
- Date: 2026-07-31

## Context

Random test data prevents collisions and broadens input coverage, but hidden global randomness makes a failed
test difficult to reproduce. A shared random generator also makes values depend on thread scheduling when JUnit
runs classes in parallel. Time-based fields such as `LocalDate.now()` introduce another source of drift.

## Decision

bookwright resolves one signed 64-bit run seed from `-Dtest.seed`, then `TEST_SEED`, or generates it once when
neither is configured. `TestDataExtension` derives an independent test seed from the run seed and the stable
JUnit unique id using SHA-256.

Each test receives its own `TestData` instance and deterministic random sequence. Factories accept that explicit
source; they do not use global random generators, UUID generators, execution order, or the current date.

The extension records the run seed, test seed, and exact Gradle replay command in Allure. Preconditions consume
the same method-scoped `TestData` instance as the test body.

## Consequences

- Parallel scheduling cannot change generated values.
- A failure can be replayed with `-Dtest.seed=<seed>` and the reported test selector.
- Different tests remain isolated even when they share a run seed.
- Factory APIs are slightly more explicit because generated data is a test dependency.
- Parameterized invocation ids must remain stable to reproduce one invocation exactly.
