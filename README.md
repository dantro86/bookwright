# bookwright

![Java 21](https://img.shields.io/badge/Java-21-orange?logo=openjdk)
![Gradle](https://img.shields.io/badge/Gradle-8.14-02303A?logo=gradle)
![JUnit 5](https://img.shields.io/badge/JUnit-5-25A162?logo=junit5&logoColor=white)
![Playwright](https://img.shields.io/badge/Playwright-1.53-2EAD33?logo=playwright)
![Retrofit](https://img.shields.io/badge/Retrofit-3.0-48B983)
![Allure](https://img.shields.io/badge/Allure-2.29-FA6C0E)
![GitHub Release](https://img.shields.io/github/v/release/dantro86/bookwright?sort=semver)
![tests](https://github.com/dantro86/bookwright/actions/workflows/tests.yml/badge.svg)

Example Java test-automation framework: API + UI + DB-over-SSH in one lean project.
Distilled from a production framework — same architectural ideas.

## Stack

Java 21 · Gradle (Kotlin DSL) · JUnit 5 (parallel) · Guice · Retrofit/OkHttp · Playwright · Allure ·
Owner (config) · Awaitility · JDBI + HikariCP · JSch (SSH tunnel) · Lombok · AssertJ

## What it tests

| Track | Target                                                        |
|-------|---------------------------------------------------------------|
| API   | [restful-booker](https://restful-booker.herokuapp.com) (or the dockerized copy on stand `local`) |
| UI    | [saucedemo.com](https://www.saucedemo.com)                    |
| DB    | Local MySQL from `docker/`, reachable **only** through an SSH tunnel via the bastion container |

## Architecture

```
test ──> Steps facade (@Step, Allure) ──> Retrofit API interface / Playwright page object / JDBI DAO
              │
              └── wired by Guice modules, injected into tests by StepsParameterResolver
```

Key mechanisms (all in `src/main/java/io/bookwright`):

- **Preconditions** — `@Preconditions({BOOKING_EXISTS})` on a test: the `Precondition` enum holds named
  setup actions, `PreconditionProvider` runs them right before the test body (each as an Allure step)
  and shares created data with the test through the JUnit method-scoped store (`TestStore` parameter).
- **Fixtures** — `@WithAuthSession` creates an explicit authentication value object before the test
  (`store.authSession()`), which authorized API operations require.
- **Teardown** — steps push a cleanup lambda into a per-test LIFO queue for every entity they create;
  `TeardownExtension` drains it after each test. `teardown.failOnError` controls whether cleanup failures
  fail an otherwise successful test; a primary test failure is never replaced.
- **Extensions** — auto-registered via `META-INF/services` + `junit-platform.properties`
  (autodetection, parallel classes, fixed parallelism 4). `UiArtifactsOnFailureExtension` attaches
  a screenshot, page HTML, Playwright trace, and browser diagnostics when a UI test fails.
- **Config** — Owner interfaces with MERGE policy: system properties > env vars >
  `stands/${STAND}/stand.properties`. Switch stands with `-DSTAND=local` (default `prod`).
  No secrets in the repo: local demo passwords are documented non-secrets, real ones come from env
  (`DB_PASSWORD`, `SSH_PASSWORD`).
- **SSH tunnel** — `SshTunnel` (JSch) forwards `localhost:13306` to MySQL through the bastion,
  opened lazily on first DB access, closed by a run-level `TestExecutionListener`. MySQL has no
  host port mapping, so the tunnel is genuinely required.
- **Waits** — UI relies on Playwright's auto-retrying assertions; async API states are polled with
  Awaitility via `Waits` (shared defaults + mandatory alias, composed fluently at the call site).
  Examples: `AuthApiSteps.waitUntilApiUp()` (infrastructure warm-up),
  `BookingApiSteps.waitUntilSearchableByName()` (eventual consistency).
- **Safe HTTP reporting** — one interceptor produces sanitized logs and Allure attachments. Sensitive
  headers, query parameters, JSON fields, and form fields are redacted; unknown body formats are omitted.
  The rationale and trade-offs are documented in [ADR 0001](docs/adr/0001-safe-http-reporting.md).
- **UI failure diagnostics** — every UI test gets an isolated tracing session and bounded event capture
  for console errors, page errors, and failed requests. Failure artifacts include the current URL and
  viewport; successful-test traces are discarded. See [ADR 0002](docs/adr/0002-ui-failure-artifacts.md).
- **User-facing UI locators** — product actions find the card by its visible product name and resolve the
  button inside that card; no selector slug is derived from display text. Assertions verify complete
  product collections and cart → checkout → completion state transitions.
- **Tags** — `@Smoke`, `@Regression`, `@Api`, `@Ui`, `@Db` wrap JUnit `@Tag`;
  `@OwnerDanil` wraps Allure `@Owner`.

## Running

```bash
# API tests (no docker needed, hits public restful-booker)
./gradlew test --tests "io.bookwright.tests.api.*"

# UI tests (Playwright downloads Chromium on first run)
./gradlew test --tests "io.bookwright.tests.ui.*"

# DB + tunnel tests and/or the local API stand
docker compose -f docker/docker-compose.yml up -d
./gradlew test --tests "io.bookwright.tests.db.*"
./gradlew test -DSTAND=local --tests "io.bookwright.tests.api.*"
docker compose -f docker/docker-compose.yml down -v

# by tags
./gradlew test -DincludeTags=smoke
./gradlew test -DincludeTags=regression -DexcludeTags=ui

# everything + report
docker compose -f docker/docker-compose.yml up -d
./gradlew clean test
allure serve build/allure-results
```

Headed browser: `./gradlew test -Dui.headless=false --tests "io.bookwright.tests.ui.*"`
(any config key can be overridden the same way — system properties beat the stand file).

## Versioning

bookwright follows [Semantic Versioning](https://semver.org/). The current version has a single source of
truth in `gradle.properties`; `./gradlew printVersion` prints it and `./gradlew validateVersion` verifies it.
Release notes are maintained in [CHANGELOG.md](CHANGELOG.md), and accepted improvements are tracked in
[ROADMAP.md](ROADMAP.md).

Releases are tag-driven. After updating `projectVersion` and moving entries from `Unreleased` to a dated
version in the changelog, push `v<projectVersion>`; CI verifies the tag, runs the full local stand, and
creates the GitHub Release from that changelog section.

## Layout

```
src/main/java/io/bookwright/
├── annotations/  tags + owners (6 annotations, not 133)
├── api/          Retrofit interfaces + RetrofitFactory (+model/ DTOs)
├── config/       Owner configs + Configs entry point
├── db/           SshTunnel, DbPool, DAO, row mapper
├── di/           Guice modules (Api, Ui, Db)
├── junit/        extensions: preconditions, fixtures, resolver, screenshots, tunnel lifecycle
├── steps/        ApiSteps / UiSteps / DbSteps facades
├── teardown/     LIFO teardown queue + extension
├── ui/           BrowserManager + page objects (plain Playwright locators)
└── util/         Calls, Waits, BookingFactory
src/test/java/io/bookwright/{teardown,tests/{api,db,framework,ui}}/
```

## Author

[**Danil Trofimov**](https://www.linkedin.com/in/dantro/)

**Senior SDET & QA Lead** · Java · Playwright · Python · CI/CD Quality Gates · AI-Powered Automation

I am a QA automation engineer turned AI-first quality systems builder, with 16 years in testing and
the last 7 focused on designing automation frameworks and scaling QA teams across fintech, iGaming,
and e-commerce.

At B2Broker, I lead test automation for B2Core, a financial platform built on Kubernetes and
microservices. I designed and built its Java automation framework from scratch with Playwright,
OkHttp, and JUnit 5, helping move the team from manual regression cycles toward continuous delivery
backed by automated quality checks.

My current focus is agentic quality engineering: Claude-powered workflows that generate tests from
code and requirements, diagnose failures, review test quality, and connect the entire process through
custom skills and MCP integrations.

I care about eliminating repetitive work through autonomous pipelines, keeping architecture clean,
and treating quality as an engineering system rather than a checklist. **bookwright** is a public,
educational expression of those principles.

[LinkedIn](https://www.linkedin.com/in/dantro/) · [GitHub](https://github.com/dantro86)
