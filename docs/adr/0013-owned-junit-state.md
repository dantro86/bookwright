# ADR 0013: Owned and Typed JUnit State

## Status

Accepted

## Context

`NamespaceRegistry` had become the owner of unrelated string keys for authentication, bookings,
test data, users, and teardown. Product tests could also call generic `TestStore.get` and `put`, so a
key typo or wrong type remained a runtime concern. Restful-booker authorization had two setup
semantics: `@WithAuthSession` and an `AUTH_SESSION` precondition.

## Decision

`NamespaceRegistry` creates scopes only. Each state producer owns and hides its key:

| State | Owner |
| --- | --- |
| restful-booker auth session | `AuthSessionExtension` |
| created booking | `Precondition.BOOKING_EXISTS` |
| deterministic data | `TestDataExtension` |
| local application user | `UserFixtureExtension` |
| cleanup queue | `TeardownStorage` |

`TestStore` exposes typed reads (`authSession`, `booking`, `testData`, and `testUser`) while generic
access is package-private infrastructure. Preconditions receive `TestStore` instead of the raw JUnit
store. `@WithAuthSession` is the single authentication-fixture contract; `@Preconditions` prepares
dependent product state. Identity selection remains the parameterized `@UserFixture(NEW|EXISTING)`
contract.

All current product preconditions create isolated state and register LIFO cleanup. A source-level
architecture check rejects exception-driven control flow in the precondition catalog; a future
find-or-create precondition must inspect a returned collection before deciding to create.

## Consequences

- State ownership is local and discoverable instead of centralized in a key registry.
- Product tests cannot exchange arbitrary string-keyed values.
- Missing fixture state fails through a typed accessor with an actionable diagnostic.
- Auth fixtures, user identity fixtures, and product-state preconditions have distinct roles.

