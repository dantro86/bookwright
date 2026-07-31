# Changelog

All notable changes to bookwright are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project follows [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

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

### Planned

- See [ROADMAP.md](ROADMAP.md) for the prioritized engineering plan.

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

[Unreleased]: https://github.com/dantro86/bookwright/compare/v1.0.0...HEAD
[1.0.0]: https://github.com/dantro86/bookwright/releases/tag/v1.0.0
