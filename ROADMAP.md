# Engineering roadmap

This roadmap records the SDET review decisions accepted for bookwright. Work is ordered by engineering risk and educational value.

## 0. Versioning and releases

- [x] Adopt Semantic Versioning with one version source in `gradle.properties`.
- [x] Add a Keep a Changelog changelog.
- [x] Validate the version and release tag in Gradle.
- [x] Add an automated, tag-driven GitHub Release workflow.
- [x] Create the initial `v1.0.0` tag and GitHub Release.
- [x] Release the lifecycle and API contract hardening as `v1.1.0`.

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
- [ ] Verify complete UI collections (for example the full sorted inventory), not one representative element.
- [ ] Replace derived selector slugs with locators scoped to the visible product/card.
- [ ] Make randomized test data reproducible with a configurable seed recorded in Allure.
- [ ] Add framework self-tests for API helpers, config precedence, preconditions, JUnit Store isolation, teardown order/error policy, waits, screenshots, and resource closure.
- [ ] Add parallel-isolation tests and use MockWebServer for deterministic HTTP edge cases.

## 3. Reproducible and secure infrastructure

- [ ] Pin Docker image versions/digests and add health checks for MySQL, SSH, and restful-booker.
- [ ] Remove fixed host/tunnel port assumptions so concurrent local runs do not conflict.
- [ ] Keep password/insecure host-key SSH only for the local demo; support private keys and known_hosts for non-local stands.
- [ ] Validate required stand configuration before opening clients or infrastructure.

## 4. Portfolio, CI, and documentation

- [ ] Split CI into framework, API, UI, and DB/SSH quality gates, then merge results.
- [ ] Publish a history-enabled Allure report to GitHub Pages and link it from README.
- [ ] Add focused quality gates: Spotless, JaCoCo for framework self-tests, dependency updates/review, CodeQL, and Gradle dependency verification.
- [ ] Add concise ADRs explaining Retrofit, plain Playwright locators, Guice, JUnit Store, LIFO cleanup, Awaitility, and the no-global-retry policy.

## 5. Integrated local system

- [ ] Add a small local booking application backed by the same MySQL database.
- [ ] Demonstrate an end-to-end API → DB verification → cleanup scenario through the SSH tunnel.
- [ ] Keep the public restful-booker profile as a lightweight external API example.

## 6. Advanced learning examples

- [ ] Add selected examples of parameterized tests, schema/contract validation, Playwright network mocking, accessibility, visual comparison, multiple browsers, dynamic tests, stand conditions, and flaky-test quarantine.
- [ ] Add these selectively: each example must explain a real use case and trade-off rather than merely add another library.
