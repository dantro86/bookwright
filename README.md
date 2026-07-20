# bookwright

![Java 21](https://img.shields.io/badge/Java-21-orange?logo=openjdk)
![Gradle](https://img.shields.io/badge/Gradle-8.14-02303A?logo=gradle)
![JUnit 5](https://img.shields.io/badge/JUnit-5-25A162?logo=junit5&logoColor=white)
![Playwright](https://img.shields.io/badge/Playwright-1.53-2EAD33?logo=playwright)
![Retrofit](https://img.shields.io/badge/Retrofit-3.0-48B983)
![Allure](https://img.shields.io/badge/Allure-2.29-FA6C0E)
![tests](https://github.com/dantro86/bookwright/actions/workflows/tests.yml/badge.svg)

Example Java test-automation framework: API + UI + DB-over-SSH in one lean project.
Distilled from a production framework — same architectural ideas.

## Stack

Java 21 · Gradle (Kotlin DSL) · JUnit 5 (parallel) · Guice · Retrofit/OkHttp · Playwright · Allure ·
Owner (config) · JDBI + HikariCP · JSch (SSH tunnel) · Lombok · AssertJ

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
- **Fixtures** — `@WithAuthSession` obtains an API token before the test (`store.authToken()`).
- **Teardown** — steps push a cleanup lambda into a per-test LIFO queue for every entity they create;
  `TeardownExtension` drains it after each test. Cleanup failures are logged, never fail the test.
- **Extensions** — auto-registered via `META-INF/services` + `junit-platform.properties`
  (autodetection, parallel classes, fixed parallelism 4). `ScreenshotOnFailureExtension` attaches
  a screenshot + page HTML to Allure when a UI test fails.
- **Config** — Owner interfaces with MERGE policy: system properties > env vars >
  `stands/${STAND}/stand.properties`. Switch stands with `-DSTAND=local` (default `prod`).
  No secrets in the repo: local demo passwords are documented non-secrets, real ones come from env
  (`DB_PASSWORD`, `SSH_PASSWORD`).
- **SSH tunnel** — `SshTunnel` (JSch) forwards `localhost:13306` to MySQL through the bastion,
  opened lazily on first DB access, closed by a run-level `TestExecutionListener`. MySQL has no
  host port mapping, so the tunnel is genuinely required.
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
└── util/         Calls, BookingFactory
src/test/java/io/bookwright/tests/{api,ui,db}/
```
