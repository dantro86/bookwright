# ADR 0001: Safe HTTP reporting by default

- Status: Accepted
- Date: 2026-07-31

## Context

HTTP diagnostics are essential in an automation framework, but common off-the-shelf interceptors attach
raw headers and bodies. In bookwright this exposed authentication passwords in request JSON, session
cookies in request headers, and issued tokens in response JSON and Allure step parameters.

Header-only redaction is insufficient because credentials can appear in headers, query parameters,
request bodies, response bodies, exception diagnostics, and object string representations.

## Decision

bookwright uses one project-owned `SafeHttpReportingInterceptor` instead of raw body logging and
`AllureOkHttp3`. It sends the original request unchanged while producing sanitized console summaries and
plain-text Allure attachments.

`SecretSanitizer` applies these rules:

- redact sensitive header and query names, including authorization, cookies, credentials, passwords,
  tokens, secrets, and API keys;
- recursively redact matching fields in JSON objects and arrays;
- redact matching form fields;
- omit unsupported, oversized, one-shot, duplex, or malformed bodies rather than risk disclosure;
- sanitize URLs and error bodies used by API exceptions;
- ensure authentication value objects do not reveal their token through `toString()`.

The sanitizer is used only for diagnostics. It never mutates the network request or response consumed by
the test.

## Consequences

- All HTTP output follows one security policy and remains useful for failure analysis.
- Unknown body formats are intentionally less observable until an explicit safe formatter is added.
- New authentication mechanisms must add redaction tests before their diagnostics are enabled.
- A clean Allure result scan can be used as a release-level defense in depth.
