# ADR 0009: Integrated local booking system

## Status

Accepted

## Context

Public demo systems are useful for showing Retrofit and Playwright, but they cannot prove that one test can cross a real application, persistence, and infrastructure boundary. A mock server would make that example deterministic while removing the behavior the example is meant to verify.

## Decision

bookwright includes a small Java application in the `local-app` Gradle module. It uses the JDK HTTP server, Jackson, HikariCP, and JDBC rather than introducing a production web framework solely for the demo.

The application later gained a deliberately small user/session boundary and protected HTML page so the framework
can demonstrate API-authenticated browser fixtures. That decision is documented separately in [ADR 0010](0010-user-fixtures-and-api-authenticated-ui.md).

The application and tests share one MySQL service on the private Compose network:

1. Retrofit creates a booking through the application's HTTP API.
2. JDBI reads the resulting row through the SSH bastion and validates every persisted field.
3. Retrofit deletes the booking through the same API boundary.
4. JDBI confirms that the row is absent.
5. A method-scoped LIFO teardown remains registered as an idempotent fallback if the scenario fails before explicit cleanup.

The application is enabled only by the Compose `integrated` profile. `run-local-tests.sh` enables that profile for `integrationTest` and the full `test` task, discovers its dynamic host port, and forwards the URL as Owner configuration.

Both build and runtime container images are pinned by digest. The runtime container is non-root and uses a Java health probe that checks application and database readiness without installing operating-system utilities.

## Consequences

- The framework demonstrates a real cross-layer test without depending on an external application.
- API and DB assertions observe the same committed state.
- Parallel local checkouts remain isolated by Compose project name and dynamic host ports.
- The Docker build is heavier than a mock server and is therefore isolated in its own CI gate and Compose profile.
- The local application is deliberately small; it is a test target, not a second framework or a production service template.
