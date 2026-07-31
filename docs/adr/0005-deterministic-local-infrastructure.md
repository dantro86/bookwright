# ADR 0005: Deterministic local infrastructure and explicit SSH trust

- Status: Accepted
- Date: 2026-07-31

## Context

Mutable Docker tags make a previously green test environment change without a repository commit. Fixed host ports
prevent concurrent checkouts from running the stand, and a fixed SSH forwarding port can collide with unrelated
local processes. Password authentication combined with `StrictHostKeyChecking=no` is acceptable for an isolated
demo container but must never become the default for a shared environment.

## Decision

All Compose images are pinned to immutable manifest digests and every service has an explicit health check.
`scripts/run-local-tests.sh` creates a uniquely named Compose project, lets Docker publish the API and SSH ports
dynamically, discovers those ports, and passes them to the Gradle test worker. JSch receives local port `0` and
uses the operating system's selected forwarding port for JDBC.

SSH has two explicit profiles:

- `PASSWORD` is accepted only for `STAND=local`, a loopback SSH host, and disabled strict host-key checking.
- `PRIVATE_KEY` requires strict host-key checking plus readable private-key and `known_hosts` files.

Infrastructure configuration is validated as one unit before JSch or Hikari opens a connection. The public `prod`
stand intentionally contains no DB or SSH credentials.

## Consequences

- Repository state identifies the exact container manifests used by CI.
- Concurrent local checkouts do not compete for API, SSH, or tunnel ports.
- Local demo credentials remain simple and clearly bounded to loopback infrastructure.
- Non-local DB tests fail before network access when trust material or required settings are missing.
- Updating a container requires an explicit digest change and a normal code review.
