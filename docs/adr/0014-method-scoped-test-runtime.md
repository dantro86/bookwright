# ADR 0014: Method-Scoped Test Runtime and Owner-Constructed Fixtures

## Status

Accepted

## Context

`StepsParameterResolver` originally resolved step facades, framework state, browser lifecycle, and
every concrete fixture type. Moving fixture factories into a separate catalog would shorten the
resolver, but it would preserve a central registry that must change whenever a product domain adds
a fixture. It would also leave API, UI, and database facades backed by separate Guice injectors.

The framework needs an extension model where adding a fixture changes only the owning domain while
all objects used by one test share the same configuration, deterministic data, and teardown queue.

## Decision

One `TestRuntime` Guice injector is created lazily in the method-scoped JUnit Store. It composes the
API, UI, and database modules and binds the test-owned `MainConfig`, `TestData`,
`TeardownStorage`, and `ExtensionContext` instances.

JUnit parameter responsibilities are separated:

| Resolver | Responsibility |
| --- | --- |
| `StepsParameterResolver` | `ApiSteps`, `UiSteps`, and `DbSteps` facades |
| `FrameworkParameterResolver` | typed JUnit-owned state such as `TestStore` and `TestUser` |
| `TestFixtureParameterResolver` | parameters explicitly marked with `@TestFixture` |
| `TestDataExtension` | reproducible `TestData` |

Fixture classes declare dependencies through an injectable constructor. The fixture resolver asks
the method runtime for the declared parameter type; there is no fixture catalog, package scan,
reflection-based factory convention, or marker interface. A new fixture therefore requires only
its immutable type, injectable constructor, and an explicit `@TestFixture` at the test boundary.

## Consequences

- Product domains own construction of their fixtures without editing framework core.
- Step facades, preconditions, user fixtures, and scenario fixtures share one per-test object graph.
- Fixture use is visible in test signatures and cannot accidentally claim arbitrary parameters.
- Invalid fixture construction fails during parameter resolution with the fixture type and its
  Guice cause.
- Browser contexts remain method-scoped and browser sessions remain class/worker-scoped; those
  resources are prepared only when `UiSteps` is requested.
