# ADR 0010: User fixtures and API-authenticated UI

## Status

Accepted

## Context

Repeatedly submitting a login form makes unrelated UI scenarios slower and couples them to authentication UI.
Using only a configured shared account creates parallel-state collisions, while creating a user without owning its
cleanup leaks data. Sauce Demo does not expose a supported authentication API, so pretending that it does would be
a misleading framework example.

## Decision

The integrated local application provides a real user boundary:

1. `@UserFixture(NEW)` creates deterministic unique credentials, registers through Retrofit, authenticates, and
   registers an idempotent user deletion in the method-scoped LIFO teardown.
2. `@UserFixture(EXISTING)` authenticates the configured reusable account without mutating or deleting it.
3. Both modes expose one typed `TestUser` containing `UserCredentials`, `UserProfile`, and `UserSession`. Secret-bearing
   values redact their `toString()` output.
4. `UserFixtureExtension` runs before test parameter resolution. `UiModule` reads the fixture from the JUnit Store,
   and `BrowserManager` adds the API-issued HTTP-only cookie before creating the page.
5. Every test still owns a fresh browser context. Browser and Playwright remain class-scoped per worker thread.
6. Sauce Demo scenarios keep their form flow because the public target has no supported authentication API. The
   framework does not encode an undocumented local-storage shortcut and misrepresent it as API authentication.

The local application stores passwords with PBKDF2-HMAC-SHA256 and random salts. It stores session tokens only as
SHA-256 hashes and gives them a one-hour expiry. These choices make the teaching example safe without turning the
small test target into an identity platform.

## Consequences

- Local product scenarios no longer depend on a login form unless login is the behavior under test.
- New users are isolated and owned by the test that created them; configured users remain reusable.
- API and UI consume the same real session, proving the handoff rather than mocking it.
- Fixture setup must finish before Playwright parameter resolution; the JUnit extension order is therefore part of
  the tested framework contract.
- The external Sauce Demo remains a documented exception rather than relying on an unstable internal storage key.
