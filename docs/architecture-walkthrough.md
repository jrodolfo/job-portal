# Architecture Walkthrough

This document is a concise walkthrough of `Job Portal`.

It is intentionally shorter and more conversational than the root
[`README.md`](../README.md), the structured
[`architecture.md`](./architecture.md), and the ADR set in [`adr/`](./adr/).

Suggested reading path:

1. start with [`../README.md`](../README.md)
2. then read [`architecture.md`](./architecture.md)
3. use this file as a practical walkthrough of the system
4. follow specific decisions into [`adr/README.md`](./adr/README.md)

## One-Minute System Summary

This repository is a full-stack job portal with two main application modules
and a root-level operations layer:

- `job-portal-frontend`: React and Vite application for browser interaction
- `job-portal-backend`: Spring Boot API for security, persistence, and business
  logic
- root-level Docker Compose, scripts, and docs that operate the stack as one system

The main architectural idea is that the backend is the system boundary. The
frontend handles user interaction, but persistence, authentication, OpenAPI,
observability configuration, and deployment-facing behavior all center on the
backend and the root orchestration files.

## Core Design Story

If I had to explain the project in a few sentences:

1. the React frontend calls the Spring Boot backend over HTTP
2. the backend authenticates the user through local auth or Google OAuth2
3. the backend persists application state in MySQL through JPA
4. Docker Compose can run the frontend, backend, database, collector, and
   Jaeger together from the repo root
5. OpenTelemetry instrumentation sends traces through the collector for local
   or EC2-style observability flows

This design was chosen so the project can demonstrate more than CRUD screens.
It can show a realistic frontend/backend split, hybrid authentication,
containerized local development, deployment preparation, and observability in
one repository.

## Main Architectural Decisions

### 1. Keep the repository as a two-module workspace

Why:

- the backend and frontend have different runtimes and toolchains
- the root of the repo still needs to coordinate them as one application stack
- the resulting layout is easier to explain during maintenance and interviews

Related ADRs:

- [ADR 0001](./adr/0001-use-a-two-module-job-portal-workspace.md)
- [ADR 0002](./adr/0002-keep-react-frontend-separate-from-spring-boot-backend.md)

### 2. Keep the backend as the primary system boundary

Why:

- persistence, auth, JWT issuance, and OpenAPI belong in one controlled place
- the frontend stays focused on UI concerns instead of absorbing backend policy
- tools like Swagger, Insomnia, and curl can use the same backend contracts

Related ADR:

- [ADR 0002](./adr/0002-keep-react-frontend-separate-from-spring-boot-backend.md)

### 3. Use MySQL as the real application database

Why:

- the runtime configuration, Docker Compose stack, and docs all target MySQL
- it keeps local and production-style behavior aligned
- test-only alternatives do not replace the real runtime persistence choice

Related ADR:

- [ADR 0003](./adr/0003-use-mysql-as-the-primary-application-database.md)

### 4. Make Docker Compose the default full-stack local runtime

Why:

- this repo is easiest to run and demo when the whole stack starts from the root
- the compose model makes observability part of the default runtime
- it keeps local setup reproducible across backend, frontend, and support services

Related ADR:

- [ADR 0004](./adr/0004-support-local-full-stack-runtime-through-docker-compose.md)

### 5. Support both local auth and Google OAuth2, with JWT in the backend flow

Why:

- the project needs simple local use and a real third-party login path
- token-based API access remains useful for tools and secured frontend calls
- this gives the repo a more realistic security story than a single auth mode

Related ADR:

- [ADR 0005](./adr/0005-use-google-oauth2-alongside-local-auth-and-jwt.md)

### 6. Treat observability as part of the architecture, not as an add-on

Why:

- local tracing and EC2 deployment both matter in this repo
- collector-based OpenTelemetry keeps export details configurable
- Jaeger makes the local story inspectable and demoable

Related ADR:

- [ADR 0006](./adr/0006-use-opentelemetry-collector-based-observability-for-local-and-ec2.md)

### 7. Keep operations and validation assets in the repository

Why:

- Insomnia collections, collector config, queries, and scripts help explain how
  the system is actually run
- they reduce the gap between code and real operational workflows

Related ADRs:

- [ADR 0007](./adr/0007-keep-local-and-prod-operations-in-separate-script-paths.md)
- [ADR 0008](./adr/0008-keep-api-exploration-and-ops-assets-alongside-the-codebase.md)

### 8. Keep schema evolution lightweight for now

Why:

- the repo still prioritizes quick full-stack startup and iterative change
- current schema evolution is handled through application startup configuration
- formal migration tooling would add discipline, but also operational overhead

Related ADR:

- [ADR 0009](./adr/0009-use-runtime-ddl-auto-update-for-now.md)

### 9. Publish multi-architecture images from the repository

Why:

- local development hardware and deployment hardware may differ
- the repo already includes `buildx` and bake-based image publishing
- EC2-style deployment is part of the documented operating model

Related ADR:

- [ADR 0010](./adr/0010-publish-multi-architecture-docker-images-from-the-repo.md)

## Key Design Points

### Clear separation of responsibilities

- frontend owns browser interaction, routing, and client state
- backend owns persistence, auth, API contracts, and business logic
- MySQL owns runtime data persistence
- root-level compose and scripts own operational orchestration
- docs own API exploration, observability config, and support assets

### Practical full-stack developer experience

This project is intentionally set up so it can be run as an integrated stack,
not just as isolated code folders. That matters for maintainability and for
showing the project credibly in an interview.

### Backend-centric security model

The frontend does not own authentication policy. The backend does. That keeps
OAuth handling, JWT issuance, and security configuration in one place.

### Observability is part of the platform story

Tracing is not documented as a future idea. It is already wired into the
repository through the Docker stack, collector config, and environment
variables.

### Local and prod paths are similar, but not identical

The repo deliberately distinguishes local and prod operations. Local prioritizes
developer convenience; prod adds stricter validation and upstream telemetry
configuration.

### Schema management is pragmatic, not fully formalized yet

The backend currently favors `ddl-auto=update` to keep setup friction low.
That is a practical choice for this repo right now, but it is also an explicit
future evolution point rather than a hidden assumption.

### Deployment portability matters

The repository already bakes in the idea that developer hardware and deployment
hardware may differ. That is why multi-architecture image publishing is part
of the documented workflow rather than a one-off release trick.

## Common Design Questions

### Why not merge the frontend into the backend?

Short answer:

- the UI and API have different runtime concerns
- keeping them separate preserves a cleaner system boundary
- the backend remains reusable outside the browser UI

Related ADR:

- [ADR 0002](./adr/0002-keep-react-frontend-separate-from-spring-boot-backend.md)

### Why use Docker Compose if the backend and frontend can run separately?

Short answer:

- because the full stack includes more than those two apps
- MySQL, tracing, and local consistency matter here
- Compose is the most honest default way to run the system end to end

Related ADR:

- [ADR 0004](./adr/0004-support-local-full-stack-runtime-through-docker-compose.md)

### Why keep both local auth and Google OAuth2?

Short answer:

- local auth keeps setup practical
- Google OAuth2 demonstrates a realistic federated login flow
- JWT support keeps the backend usable for API clients and browser flows

Related ADR:

- [ADR 0005](./adr/0005-use-google-oauth2-alongside-local-auth-and-jwt.md)

### Why not hide observability behind external platform tooling only?

Short answer:

- the repository benefits from a runnable local tracing story
- the collector keeps the export path configurable across environments
- this project is easier to maintain when observability is explicit in the repo

Related ADR:

- [ADR 0006](./adr/0006-use-opentelemetry-collector-based-observability-for-local-and-ec2.md)

### Why keep Insomnia, queries, and collector configs inside `docs/`?

Short answer:

- they are part of how the project is validated and operated
- they make the repository more self-contained and explainable
- they help future maintenance and interview walkthroughs

Related ADR:

- [ADR 0008](./adr/0008-keep-api-exploration-and-ops-assets-alongside-the-codebase.md)

## Future Evolution Paths

If the system needs to evolve, likely next steps would be:

- introduce a stronger schema migration strategy than runtime `ddl-auto=update`
- formalize deployment beyond EC2-oriented scripts if infrastructure grows
- add richer CI coverage for frontend and backend verification
- decide whether auth should converge on one primary model
- deepen observability beyond tracing-only emphasis if operational needs expand

## Where To Read More

- Project overview and runtime instructions: [`../README.md`](../README.md)
- Structured architecture reference: [`architecture.md`](./architecture.md)
- ADR index: [`adr/README.md`](./adr/README.md)
- Backend-specific notes: [`../job-portal-backend/README.md`](../job-portal-backend/README.md)
- API exploration assets: [`insomnia/README.md`](./insomnia/README.md)
