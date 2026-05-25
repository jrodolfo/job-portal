# ADR 0002: Keep React Frontend Separate from Spring Boot Backend

- Status: `accepted`
- Date: `2026-05-25`

## Context

The application needs an interactive browser experience for applicants and
admins, while the backend must handle persistence, authentication, business
logic, and API contracts.

This repo already uses React/Vite on the frontend and Spring Boot on the
backend, and local/dev/prod operations assume that split.

## Decision

Keep the React frontend as a separate application that talks to the Spring Boot
backend over HTTP APIs rather than collapsing the UI into the backend.

## Rationale

This keeps the browser-facing concerns isolated from backend concerns.

The frontend can focus on routing, forms, client state, and API consumption,
while the backend owns persistence, security, JWT issuance, OAuth handling, and
OpenAPI exposure.

That separation also makes the backend reusable for tools such as Insomnia,
curl, and Swagger UI, not just the bundled frontend.

## Consequences

The frontend remains lightweight and framework-appropriate for client-side
interaction.

The backend remains the clear system boundary for security, persistence, and
runtime policy.

The tradeoff is that local development and deployment have to coordinate two
separate applications and the CORS/auth flow between them.

## Revisit Triggers

Reconsider this decision if the product moves to server-rendered HTML, if the
frontend becomes very thin and mostly static, or if a different UI delivery
model would materially simplify authentication and deployment.
