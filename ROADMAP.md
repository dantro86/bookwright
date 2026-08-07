# Engineering roadmap

This roadmap records the SDET review decisions accepted for bookwright. Work is ordered by engineering risk and educational value.

## 0. Versioning and releases

- [x] Adopt Semantic Versioning with one version source in `gradle.properties`.
- [x] Add a Keep a Changelog changelog.
- [x] Validate the version and release tag in Gradle.
- [x] Add an automated, tag-driven GitHub Release workflow.
- [x] Create the initial `v1.0.0` tag and GitHub Release.
- [x] Release the lifecycle and API contract hardening as `v1.1.0`.
- [x] Release secure diagnostics, stronger UI contracts, and reproducible test data as `v1.2.0`.
- [x] Release framework verification, hardened local infrastructure, quality gates, and Allure Pages as `v1.3.0`.

## 1. Correctness and lifecycle

- [x] Replace the string token in JUnit Store with an explicit `AuthSession` value object and pass it to authorized operations.
- [x] Move teardown state from `ThreadLocal` to a method-scoped JUnit Store resource.
- [x] Add `teardown.failOnError=true|false`; preserve the primary failure and report cleanup failures correctly.
- [x] Add a run/class-scoped `CloseableResource` that closes `Browser` and then `Playwright`; continue closing each context after its test.
- [x] Restrict Awaitility exception handling to known transient failures; programming errors must fail immediately.
- [x] Introduce a small `ApiCallException`/`UnexpectedResponseException` and focused `body`, `response`, and `expectStatus` helpers.
- [x] Design and implement HTTP/Allure secret redaction with safe diagnostics and regression tests.

## 2. Diagnostics and test quality

- [x] Attach Playwright trace, browser console errors, page errors, failed network requests, URL, and viewport on UI failures.
- [x] Verify complete UI collections (for example the full sorted inventory), not one representative element.
- [x] Replace derived selector slugs with locators scoped to the visible product/card.
- [x] Make randomized test data reproducible with a configurable seed recorded in Allure.
- [x] Add framework self-tests for API helpers, config precedence, preconditions, JUnit Store isolation, teardown order/error policy, waits, UI artifacts, and resource closure.
- [x] Add parallel-isolation tests and use MockWebServer for deterministic HTTP edge cases.

## 3. Reproducible and secure infrastructure

- [x] Pin Docker image versions/digests and add health checks for MySQL, SSH, and restful-booker.
- [x] Remove fixed host/tunnel port assumptions so concurrent local runs do not conflict.
- [x] Keep password/insecure host-key SSH only for the local demo; support private keys and known_hosts for non-local stands.
- [x] Validate required stand configuration before opening clients or infrastructure.

## 4. Portfolio, CI, and documentation

- [x] Split CI into framework, API, UI, and DB/SSH quality gates, then merge results.
- [x] Publish a history-enabled Allure report to GitHub Pages and link it from README.
- [x] Add focused quality gates: Spotless, JaCoCo for framework self-tests, dependency updates/review, CodeQL, and Gradle dependency verification.
- [x] Add concise ADRs explaining Retrofit, plain Playwright locators, Guice, JUnit Store, LIFO cleanup, Awaitility, and the no-global-retry policy.

## 5. Integrated local system

- [x] Add a small local booking application backed by the same MySQL database.
- [x] Demonstrate an end-to-end API → DB verification → cleanup scenario through the SSH tunnel.
- [x] Add explicit user-fixture modes for a newly registered user and a configured existing user.
- [x] Introduce user lifecycle management: create unique users through the API, expose typed credentials/session data, and register cleanup where the environment permits it.
- [x] Authenticate UI fixtures through the API and inject cookies, local storage, or Playwright storage state into a fresh browser context.
- [x] Keep form-based login out of unrelated local-application scenarios; the external Sauce Demo examples retain it because that demo exposes no supported authentication API.
- [x] Verify fresh-user and existing-user isolation under parallel execution, including missing or rejected API sessions.
- [ ] Keep the public restful-booker profile as a lightweight external API example.

## 6. Domain ownership and fixture architecture

The audit baseline is the system bookwright actually tests, not a copied enterprise domain list:

- restful-booker: health, authentication, and bookings;
- integrated local application: authentication/sessions, users, and bookings;
- Sauce Demo UI: login, inventory, and checkout;
- integrated local UI: authenticated bookings;
- database: bookings and rooms.

### 6.1 Domain boundaries

- [x] Move Retrofit clients into target/domain packages so similarly named external and local domains cannot be confused.
- [x] Split restful-booker health checks out of `AuthApi` into `HealthApi`; keep token creation in `AuthApi`.
- [x] Split `LocalUserApi` into local `AuthApi`/session operations and `UsersApi` lifecycle operations.
- [x] Keep booking clients separate per target and use consistent plural resource naming.
- [x] Mirror every API boundary in focused `HealthSteps`, `AuthSteps`, `BookingSteps`, and `UserSteps`; keep `ApiSteps` as a compact access facade only.
- [x] Split `UiSteps` into login, inventory, checkout, and local-bookings domains; keep `UiSteps` only as a compact facade if one access point remains useful.
- [x] Add architecture self-tests that reject catch-all API/steps classes and unintended cross-domain dependencies.
- [x] Remove superseded API and steps classes after migration and verify that no imports or reflection-based registrations still reference them.

### 6.2 Test data and fixtures

- [x] Move Sauce Demo accounts, expected catalog, checkout customer, sort options, and authentication-error inputs into typed fixture objects.
- [x] Move scenario values such as patch payloads, invalid credentials, product selection, and nonexistent identifiers into `TestData` or focused fixture factories.
- [x] Keep product assertions explicit while separating expected product fixtures from UI action steps.
- [x] Make steps accept ready credentials, request models, expected values, or typed fixtures; steps must not invent scenario-specific data.
- [x] Add source-level architecture checks for credentials, emails, passwords, and scenario payload literals inside steps and product tests.

### 6.3 Preconditions and typed state

- [ ] Define the boundary explicitly: identity selection (`NEW`/`EXISTING`) remains a parameterized user fixture, while dependent product state is prepared through `@Preconditions`.
- [ ] Consolidate API authorization setup so tests use one precondition/fixture path instead of combining `@WithAuthSession` and `AUTH_SESSION` semantics.
- [ ] Add typed booking and fixture accessors to `TestStore`; product tests must not call generic string-key access.
- [ ] Move `AUTH_SESSION_KEY` to `AuthSessionExtension`, booking state ownership to the booking precondition/fixture, `TEST_DATA_KEY` to `TestDataExtension`, `TEST_USER_KEY` to `UserFixtureExtension`, and `TEARDOWN_STORAGE_KEY` to `TeardownStorage`.
- [ ] Reduce `NamespaceRegistry` to class-scoped and method-scoped namespace creation only.
- [ ] Make generic `TestStore.get/put` internal after all test-facing state has typed accessors.
- [ ] For find-or-create preconditions, query first and branch on the returned collection; do not use expected exceptions as normal control flow.
- [ ] Preserve method-scoped ownership and LIFO cleanup for every entity created by a precondition or fixture.

### 6.4 Wait contracts and diagnostics

- [ ] Return a matched value directly from Awaitility `until` whenever the caller needs it; never use arrays, atomics, or mutable holders as lambda return channels.
- [ ] Keep terminal/error-state classification in named methods and fail immediately when polling reaches a terminal failure.
- [ ] Refactor searchable-booking polling to return the matched booking identifier instead of discarding the resolved result.
- [ ] Introduce a safe `RequiredEntityNotFoundException` with entity type, criterion, endpoint/query source, and returned collection size.
- [ ] Replace required DB/API lookups that return `null` with required lookup methods; retain nullable/optional methods only when absence is an expected contract.
- [ ] Add a business-operation exception that preserves the original `ApiCallException` or `UnexpectedResponseException` as its cause.
- [ ] Wrap create/update/delete step failures with operation context while retaining HTTP status, method, sanitized URL, and sanitized response body from the cause.
- [ ] Replace cleanup-specific generic `IllegalStateException` diagnostics with the same safe API response contract and operation context.
- [ ] Add regression tests proving that cause chains remain intact and that passwords, cookies, tokens, and secrets never reach exceptions, logs, Allure parameters, or `toString()`.

### 6.5 Refactoring verification

- [x] Document the final domain/state ownership map in an ADR before moving packages.
- [x] Preserve behavior and LIFO cleanup with characterization tests before deleting old classes.
- [x] Run Spotless, framework self-tests, product smoke tests, integrated API/UI/DB scenarios, and `git diff --check` after each migration slice.
- [ ] Release the domain ownership, typed fixtures, and diagnostic hardening as the next minor version.

## 7. Advanced learning examples

- [ ] Add selected examples of parameterized tests, schema/contract validation, Playwright network mocking, accessibility, visual comparison, multiple browsers, dynamic tests, stand conditions, and flaky-test quarantine.
- [ ] Add these selectively: each example must explain a real use case and trade-off rather than merely add another library.
