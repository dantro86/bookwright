# Framework self-tests

bookwright tests its own infrastructure separately from the product scenarios. These tests protect lifecycle,
diagnostic, and failure-handling contracts that ordinary API or UI tests may exercise without proving directly.

| Contract | Self-test |
|---|---|
| API transport and response contracts | `CallsTest` |
| HTTP secret redaction and fail-closed body handling | `SafeHttpReportingInterceptorTest` |
| Explicit auth-session safety | `AuthSessionTest` |
| Configuration source order, overrides, and defaults | `MainConfigTest` |
| Precondition order and method-store handoff | `PreconditionProviderTest` |
| JUnit method-store isolation | `StoreIsolationTest` |
| LIFO teardown and error policy | `TeardownExtensionTest` |
| Awaitility transient-failure policy and diagnostics | `WaitsTest` |
| Deterministic and parallel-independent test data | `TestSeedsTest` |
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
