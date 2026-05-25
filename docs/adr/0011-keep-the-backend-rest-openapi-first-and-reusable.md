# ADR 0011: Keep the Backend REST/OpenAPI-First and Reusable

- Status: `accepted`
- Date: `2026-05-25`

## Context

The backend in this repository is not used only by the bundled frontend.

The repo already includes multiple ways to interact with the backend directly:

- Swagger/OpenAPI exposure from the Spring Boot app
- Insomnia assets under `docs/insomnia`
- curl-oriented examples in the main README

That means the backend is already serving as a reusable application boundary,
not just a thin transport layer for the React UI.

## Decision

Keep the backend REST/OpenAPI-first and treat it as a reusable API surface that
can be consumed by:

- the bundled React frontend
- Swagger/OpenAPI users
- Insomnia collections
- curl and other HTTP clients

Do not narrow the backend into a frontend-only private interface.

## Rationale

This choice fits how the repository is actually operated and explained.

The frontend remains important, but the backend already carries the real system
contracts for persistence, authentication, authorization, and business logic.
Keeping those contracts explicit and externally usable makes the project easier
to debug, demo, test, and discuss.

It also strengthens the architectural separation between UI concerns and
application-service concerns.

## Consequences

The backend remains independently testable and inspectable outside the browser
UI.

The repository can support multiple consumption paths without pretending the
frontend is the only valid client.

The downside is that API clarity and backward compatibility matter more, since
the backend is serving more than one usage path.

## Revisit Triggers

Reconsider this decision if the product becomes tightly coupled to a single UI
delivery model, if the backend shifts to a different API style entirely, or if
the repo no longer benefits from exposing and documenting reusable HTTP
contracts.
