# ADR 0005: Use Google OAuth2 Alongside Local Auth and JWT

- Status: `accepted`
- Date: `2026-05-25`

## Context

The application needs to support more than one authentication path:

- local username/password access for development and application flows
- Google-based login for OAuth/OpenID Connect scenarios
- JWT issuance for bearer-token-based API use after authentication

The repository documentation and backend configuration already reflect those
multiple auth paths.

## Decision

Keep a hybrid authentication model:

- local credentials remain available
- Google OAuth2 login is supported through Spring Security OAuth client setup
- JWT tokens are issued and used for backend API access where appropriate

## Rationale

This model fits how the repository is actually used.

It allows simple local development, demonstrates modern third-party login
integration, and supports token-based API access for tools and browser clients.

That combination is more useful for learning, demos, and interview discussion
than a single auth mode would be.

## Consequences

The system can demonstrate both traditional application login and federated
identity flow.

Swagger, Insomnia, and browser clients can all interact with the backend in a
way that reflects real application security concerns.

The downside is added complexity in security configuration, environment setup,
and debugging auth-related flows.

## Revisit Triggers

Reconsider this decision if the app standardizes on a single identity provider,
if local auth is intentionally removed, or if JWT issuance is replaced by a
different token/session model.
