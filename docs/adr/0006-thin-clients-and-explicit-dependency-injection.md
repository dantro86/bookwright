# ADR 0006: Thin clients and explicit dependency injection

- Status: Accepted
- Date: 2026-07-31

## Context

Test frameworks often hide mature libraries behind request, response, driver, and service abstractions. Those layers make simple scenarios harder to read and rarely provide a real alternative implementation.

## Decision

Use Retrofit interfaces as the HTTP contract, OkHttp as the configured transport, and plain Playwright `Locator` objects in page objects. Guice wires concrete step facades and their dependencies, while JUnit parameter resolvers define the test-facing injection boundary.

Do not add framework-owned HTTP or element wrappers unless a second implementation creates a proven need.

## Consequences

- Tests expose the capabilities and terminology of the underlying libraries.
- Cross-cutting configuration remains centralized without duplicating each library's API.
- Guice modules remain small and track-specific instead of becoming one global container.
- Replacing a core library is an explicit migration rather than a promise hidden behind leaky interfaces.
