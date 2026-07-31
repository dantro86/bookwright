# Changelog

All notable changes to bookwright are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project follows [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added

- Added a documented framework verification matrix and self-tests for configuration, preconditions, JUnit Store isolation, waits, UI artifact isolation, and browser resource closure.

### Planned

- See [ROADMAP.md](ROADMAP.md) for the prioritized engineering plan.
- Add managed new/existing user fixtures and API-authenticated Playwright contexts for UI scenarios.

## [1.2.0] - 2026-07-31

### Added

- Added safe HTTP request/response reporting with redaction tests and an architecture decision record.
- Added Playwright traces and bounded browser-event diagnostics to failed UI test artifacts.
- Added UI diagnostics self-tests and an architecture decision record for artifact lifecycle and security.
- Added a UI scenario proving that products with punctuation are selected by their visible name.
- Added deterministic per-test data generation with configurable run seeds, Allure replay metadata, parallel-isolation tests, and ADR 0003.

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

- Added an explicit `AuthSession` value object for authorized API operations.
- Added configurable teardown failure handling through `teardown.failOnError`.
- Added focused API transport and response-contract exceptions with deterministic MockWebServer tests.
- Added framework self-tests for authentication sessions and teardown order/error policy.

### Changed

- Added a dynamic GitHub Release badge and marked the initial release milestone complete.
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

[Unreleased]: https://github.com/dantro86/bookwright/compare/v1.2.0...HEAD
[1.2.0]: https://github.com/dantro86/bookwright/releases/tag/v1.2.0
[1.1.0]: https://github.com/dantro86/bookwright/releases/tag/v1.1.0
[1.0.0]: https://github.com/dantro86/bookwright/releases/tag/v1.0.0
