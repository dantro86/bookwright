# ADR 0002: Capture rich UI diagnostics only on failure

- Status: Accepted
- Date: 2026-07-31

## Context

A screenshot and page HTML are useful but often insufficient for diagnosing UI failures. They do not
explain JavaScript exceptions, browser console errors, failed network requests, or the actions that led
to the final page state. Recording every artifact for every successful test, however, increases storage,
report noise, and exposure of potentially sensitive browser data.

Failure artifacts must also be captured before the per-test `BrowserContext` is closed. JUnit watcher
callbacks run too late for that lifecycle.

## Decision

Each bookwright UI test receives:

- a fresh Playwright `BrowserContext` and `Page`;
- a trace started with screenshots, DOM snapshots, and source capture;
- bounded per-test collections for console errors, page errors, and failed requests.

`UiArtifactsOnFailureExtension` runs as an `AfterTestExecutionCallback`, while the context and Allure test
case are still active. On failure it independently attempts to attach:

1. a full-page PNG screenshot;
2. current page HTML;
3. a text report containing URL, viewport, console errors, page errors, and failed requests;
4. a Playwright trace ZIP.

One failed artifact must not prevent the others from being captured or replace the primary test failure.
On success the trace is stopped and discarded during normal context cleanup. Browser and Playwright
processes remain class-scoped resources and are closed in that order after their owning test class.

## Consequences

- UI failures carry enough evidence for offline diagnosis with Playwright Trace Viewer.
- Successful runs avoid storing trace archives and unnecessary report noise.
- Event collections are capped to prevent a noisy page from producing unbounded memory or attachments.
- Traces, screenshots, and HTML may contain application data. Allure access and artifact retention must
  follow the security policy of the tested environment.
- The extension is failure-safe: artifact capture errors are logged and never change the test outcome.
