# Changelog

All notable changes to bookwright are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project follows [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added

- Explicit target/domain API and steps boundaries enforced by architecture self-tests and ADR 0011.
- Typed Sauce Demo, local-user, and database fixtures with source-level ownership checks and ADR 0012.

- A minimal Java booking application backed by the same private MySQL service used by database tests.
- An API → DB-over-SSH verification → API cleanup scenario with deterministic data and LIFO fallback cleanup.
- An independent integration quality gate whose results are merged into the published Allure report.
- `NEW` and `EXISTING` user fixtures with typed, secret-safe credentials, profiles, and API sessions.
- Local user registration, PBKDF2 password storage, hashed session tokens, authenticated cleanup, and a protected UI page.
- Playwright session-cookie injection and concurrent integration scenarios for new, existing, missing, and rejected sessions.
- A changelog style gate that prevents release-note bullets from repeating their section heading.

### Changed

- Split restful-booker health, auth, and bookings from local auth, users, and bookings, with compact target facades for API and UI tests.
- Replaced the monolithic UI steps class with focused Sauce Demo login, inventory, and checkout domains plus local booking UI steps.
- Moved scenario selection out of steps and product tests into immutable fixtures or reproducible per-test data.

- The isolated local launcher now supports an opt-in `integrated` Compose profile and dynamically discovers the application port.
- Unrelated local-application scenarios no longer submit a login form; Sauce Demo examples retain it because the public target exposes no supported authentication API.

### Planned

- See [ROADMAP.md](ROADMAP.md) for the prioritized engineering plan.

## [1.3.0] - 2026-08-01

### Added

- A documented framework verification matrix and self-tests for configuration, preconditions, JUnit Store isolation, waits, UI artifact isolation, and browser resource closure.
- Concurrent isolation tests for JUnit Store, Guice facades, auth data, teardown queues, generated data, and Playwright contexts.
- Deterministic MockWebServer coverage for disconnects, timeouts, malformed JSON, explicit polling, and unexpected responses.
- ADR 0004 documenting the explicit retry policy.
- Digest-pinned local services with explicit MySQL, SSH, and API health checks.
- A dynamic-port local test launcher with isolated Compose projects and guaranteed cleanup.
- Validated local-password and strict private-key SSH profiles, documented in ADR 0005.
- Independent static, framework, API, UI, and DB-over-SSH CI quality gates with one required aggregate status.
- A history-enabled merged Allure report published through GitHub Pages.
- Spotless formatting, a 60% JaCoCo framework-core coverage gate, Gradle dependency verification, Dependabot, dependency review, and CodeQL.
- ADRs for thin Retrofit/Playwright clients with Guice, JUnit-owned lifecycle and LIFO cleanup, and explicit wait boundaries.
- A CI guide covering gate contracts, branch protection, local commands, and checksum maintenance.

### Changed

- Disabled shared OkHttp connection retries so every Retrofit call executes once unless a step explicitly polls a transient state.
- Registered class-scoped browser cleanup per worker thread to support concurrent test methods without leaking sessions.
- Removed fixed API, SSH, and JDBC tunnel ports from the local execution path.
- Removed demo DB and SSH credentials from the public `prod` stand.
- Upgraded GitHub Actions to Node.js 24-based major versions where available.
- Established Google Java Format as the repository-wide source baseline.

## [1.2.0] - 2026-07-31

### Added

- Safe HTTP request/response reporting with redaction tests and an architecture decision record.
- Playwright traces and bounded browser-event diagnostics for failed UI tests.
- UI diagnostics self-tests and an architecture decision record for artifact lifecycle and security.
- A UI scenario proving that products with punctuation are selected by their visible name.
- Deterministic per-test data generation with configurable run seeds, Allure replay metadata, parallel-isolation tests, and ADR 0003.

### Changed

- Expanded the author profile to document the production, leadership, and AI-first quality engineering experience behind bookwright.
- Replaced raw OkHttp body logging and `AllureOkHttp3` attachments with sanitized diagnostics.
- Protected authentication values in Allure step parameters, API exceptions, and object string representations.
- Replaced screenshot-only UI failure handling with independent screenshot, HTML, diagnostics, and trace capture.
- Replaced derived product selector slugs with product-card scoped locators.
- Strengthened login, sorting, cart, checkout overview, and order completion assertions.
- Replaced global UUID, thread-local random, and current-date test data with explicit per-test `TestData` sequences.

## [1.1.0] - 2026-07-31

### Added

- An explicit `AuthSession` value object for authorized API operations.
- Configurable teardown failure handling through `teardown.failOnError`.
- Focused API transport and response-contract exceptions with deterministic MockWebServer tests.
- Framework self-tests for authentication sessions and teardown order/error policy.

### Changed

- The README now displays a dynamic GitHub Release badge, and the initial release milestone is complete.
- Moved teardown ownership from `ThreadLocal` state to the method-scoped JUnit Store.
- Closed browser sessions through class-scoped JUnit resources while preserving per-test browser contexts.
- Restricted Awaitility retries to transport-level `ApiCallException` failures.
- Replaced generic Retrofit execution helpers with focused `body`, `response`, and `expectStatus` operations.

## [1.0.0] - 2026-07-31

### Added

- Retrofit/OkHttp API tests against restful-booker with CRUD, negative, search, PATCH, and health scenarios.
- Playwright UI tests against Sauce Demo with isolated browser contexts and failure screenshots/HTML in Allure.
- JDBI/HikariCP database tests through a real JSch SSH tunnel to a Dockerized MySQL instance.
- Declarative `@Preconditions`, `@WithAuthSession`, method-scoped `TestStore`, and auto-registered JUnit 5 extensions.
- Per-test LIFO teardown actions for API and database entities.
- Owner configuration with system property, environment variable, and stand-file precedence.
- Awaitility presets with named waits for infrastructure warm-up and eventual consistency.
- Guice-wired `ApiSteps`, `UiSteps`, and `DbSteps` facades.
- Allure request/response reporting, owner labels, tags, parallel execution, and GitHub Actions CI.
- Reproducible local Docker stand with restful-booker, an SSH bastion, and MySQL seed data.
- MIT license, project documentation, and author/contact information.
- Semantic Versioning, a validated single version source, Keep a Changelog, and tag-driven GitHub Releases.

[Unreleased]: https://github.com/dantro86/bookwright/compare/v1.3.0...HEAD
[1.3.0]: https://github.com/dantro86/bookwright/releases/tag/v1.3.0
[1.2.0]: https://github.com/dantro86/bookwright/releases/tag/v1.2.0
[1.1.0]: https://github.com/dantro86/bookwright/releases/tag/v1.1.0
[1.0.0]: https://github.com/dantro86/bookwright/releases/tag/v1.0.0
