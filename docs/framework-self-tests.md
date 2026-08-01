# Framework self-tests

bookwright tests its own infrastructure separately from the product scenarios. These tests protect lifecycle,
diagnostic, and failure-handling contracts that ordinary API or UI tests may exercise without proving directly.

| Contract | Self-test |
|---|---|
| API transport and response contracts | `CallsTest` |
| No-global-retry policy and explicit transient polling | `RetrofitFactoryTest`, `CallsTest` |
| HTTP secret redaction and fail-closed body handling | `SafeHttpReportingInterceptorTest` |
| Explicit auth-session safety | `AuthSessionTest` |
| Typed user credentials/session redaction | `UserSessionTest` |
| Configuration source order, overrides, and defaults | `MainConfigTest` |
| Local-only password SSH and strict private-key profiles | `InfrastructureConfigValidatorTest` |
| Precondition order and method-store handoff | `PreconditionProviderTest` |
| JUnit method-store isolation | `StoreIsolationTest` |
| Concurrent Guice facade and Playwright context isolation | `StoreIsolationTest`, `BrowserContextIsolationTest` |
| LIFO teardown and error policy | `TeardownExtensionTest` |
| Concurrent auth, generated-data, and teardown isolation | `ParallelStateIsolationTest` |
| Awaitility transient-failure policy and diagnostics | `WaitsTest` |
| Deterministic and parallel-independent booking/user data | `TestSeedsTest` |
| Browser-before-Playwright resource closure | `BrowserManagerTest` |
| Bounded and sanitized browser diagnostics | `UiDiagnosticsTest` |
| Independent capture of UI failure artifacts | `UiArtifactsOnFailureExtensionTest` |

## Testing approach

- Pure contracts use fast unit tests and no external services.
- HTTP edge cases use `MockWebServer` instead of a public endpoint.
- Lifecycle primitives are kept package-private and tested in their owning package; no public API is added only
  for testing.
- Product-level API, UI, and DB tests remain responsible for end-to-end integration of the same components.
- The full CI run uses the local Docker stand, so framework self-tests and product scenarios execute together.

This split keeps failures actionable: a framework contract failure points to infrastructure code, while a product
scenario failure points to the exercised API, UI, database, or integration path.
