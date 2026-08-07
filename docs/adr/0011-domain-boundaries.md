# ADR 0011: Target and Domain Boundaries

## Status

Accepted

## Context

Bookwright exercises multiple independent systems. Flat API and steps packages made similarly named
operations ambiguous and allowed unrelated responsibilities to accumulate in the same class. In
particular, the restful-booker authentication client also owned health checks, while the local user
client also owned session creation.

## Decision

Every client and steps class is owned first by its target system and then by one product domain:

| Target | Domains |
| --- | --- |
| restful-booker API | health, auth, bookings |
| local booking API | auth, users, bookings |
| Sauce Demo UI | login, inventory, checkout |
| local booking UI | bookings |

Retrofit interfaces live below `api.<target>.<domain>`. Step classes mirror the same ownership below
`steps.<target>.<domain>` and `steps.ui.<target>.<domain>`. `ApiSteps` and `UiSteps` expose compact
target facades only; they do not contain product operations themselves.

Shared transport, sanitization, response contracts, and wire models remain target-neutral because
they do not own product behavior. Cross-domain workflows are composed explicitly by tests or a
dedicated workflow object, never hidden inside a domain step.

Architecture self-tests enforce these package and facade boundaries. Adding a new endpoint requires
placing it in its owning domain client and exposing it through the matching domain steps.

## Consequences

- Calls reveal their target and domain, for example `api.restfulBooker().bookings()`.
- Health, authentication, user lifecycle, and booking behavior evolve independently.
- The top-level facades stay stable and small as domains are added.
- Tests contain slightly longer access chains in exchange for explicit ownership.

