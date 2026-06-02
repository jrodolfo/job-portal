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
- the resulting layout is easier to explain during maintenance and technical reviews

Related ADRs:

- [ADR 0001](./adr/0001-use-a-two-module-job-portal-workspace.md)
- [ADR 0002](./adr/0002-keep-react-frontend-separate-from-spring-boot-backend.md)

### 2. Keep the backend as the primary system boundary

Why:

- persistence, auth, JWT issuance, and OpenAPI belong in one controlled place
- the frontend stays focused on UI concerns instead of absorbing backend policy
- tools like Swagger, Insomnia, and curl can use the same backend contracts

Related ADRs:

- [ADR 0002](./adr/0002-keep-react-frontend-separate-from-spring-boot-backend.md)
- [ADR 0011](./adr/0011-keep-the-backend-rest-openapi-first-and-reusable.md)

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

### 8. Manage schema evolution with versioned migrations

Why:

- the repo still prioritizes quick full-stack startup and iterative change
- versioned migrations make schema changes explicit and reviewable
- Hibernate validation still protects startup from drifting away from the expected model

Related ADR:

- [ADR 0012](./adr/0012-use-flyway-with-hibernate-validation-for-schema-management.md)

### 9. Publish multi-architecture images from the repository

Why:

- local development hardware and deployment hardware may differ
- the repo already includes `buildx` and bake-based image publishing
- GitHub Actions publishes both backend and frontend Docker Hub images automatically
- EC2-style deployment is part of the documented operating model

Related ADR:

- [ADR 0010](./adr/0010-publish-multi-architecture-docker-images-from-the-repo.md)

## Key Design Points

### Clear separation of responsibilities

- frontend owns browser interaction, routing, and client state
- backend owns persistence, auth, API contracts, and business logic
- MySQL owns runtime data persistence
- root-level compose and scripts own operational orchestration
- docs own API exploration, observability config, and operational assets

### Practical full-stack developer experience

This project is intentionally set up so it can be run as an integrated stack,
not just as isolated code folders. That matters for maintainability and for
explaining the project credibly to other engineers and reviewers.

### Job and application lifecycle rules are now intentional

The repository no longer treats apply, withdraw, reapply, review, close,
reopen, and delete-blocking behavior as incidental controller logic. Those
flows now form an explicit lifecycle model shared by the backend and frontend.

In practice, that shows up in the UI as:

- applicant job cards that surface application status plus `Applied On` and
  `Last Updated` when meaningful
- an admin dashboard that separates `Jobs`, `Add Job`, `Applications`, and
  `Users` workflows
- grouped admin application review sections with filtering, status updates, and
  lifecycle summaries
- applicant-only admin user management, where admins can list users and enable
  or disable applicant access

Related ADR:

- [ADR 0013](./adr/0013-define-job-and-application-lifecycle-rules.md)
- [ADR 0014](./adr/0014-limit-admin-user-management-to-applicants.md)

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

### Schema management is explicit, but still pragmatic

The backend now uses Flyway to apply a baseline schema migration and Hibernate
validation to verify that the runtime model still matches the database shape.
That gives the repo explicit schema history without giving up the fast startup
story it still needs for local work.

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
- [ADR 0011](./adr/0011-keep-the-backend-rest-openapi-first-and-reusable.md)

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
- they help future maintenance and technical walkthroughs

Related ADR:

- [ADR 0008](./adr/0008-keep-api-exploration-and-ops-assets-alongside-the-codebase.md)

## Future Evolution Paths

If the system needs to evolve, likely next steps would be:

- break the current schema baseline into a longer-lived incremental migration history as the data model grows
- formalize deployment beyond the current Linux host deployment scripts if infrastructure grows
- add richer CI coverage for frontend and backend verification
- decide whether auth should converge on one primary model
- deepen observability beyond tracing-only emphasis if operational needs expand

## Presentation Framing

If I needed to present this project to another engineer or reviewer, I would
emphasize four things:

- it is a real full-stack system, not just isolated frontend or backend code
- the backend is the primary architectural boundary for persistence, security,
  and API contracts
- the project includes operational concerns such as Docker Compose,
  multi-architecture images, Linux host deployment, and tracing
- the documentation now captures both the current design and the reasoning
  behind the main long-lived decisions

### What makes it technically interesting

- hybrid authentication with local auth, Google OAuth2, and JWT
- OpenTelemetry collector-based observability instead of only app-local logging
- a repo structure that supports both local full-stack work and Linux host deployment
- a backend that remains reusable through REST/OpenAPI, not only through the bundled UI

### What tradeoffs were made

- Flyway plus Hibernate validation gives clearer schema control, but it introduces migration maintenance overhead
- Docker Compose improves reproducibility, but adds operational moving parts
- keeping frontend and backend separate improves clarity, but requires cross-app coordination
- multi-architecture publishing improves portability, but complicates the build path

### What I would improve next

- **Evolve the migration strategy beyond the initial baseline:** The project now has explicit schema management through Flyway, which is a meaningful improvement over runtime schema mutation. The next step would be to keep future database changes in smaller, incremental migrations and to separate structural changes from any optional bootstrap data concerns. That would make the schema history easier to review, safer to promote across environments, and simpler to reason about when production data changes become more common.
- **Expand CI to verify both frontend and backend behavior more deeply:** The repository already has a useful baseline CI story, but a stronger pipeline would give more confidence that architectural boundaries are still working as expected. I would extend CI to run backend tests, frontend tests, and a small integration-level verification path so changes in authentication, API contracts, or container orchestration are caught earlier. The reason is simple: once a project spans UI, API, persistence, and deployment assets, shallow CI stops being enough.
- **Add richer architecture references to specific backend packages and frontend flows:** The current architecture docs describe the system well at the component level, but they could become even more actionable by mapping major concepts to exact implementation areas. For example, I would tie the security discussion to the Spring Security configuration classes, the persistence discussion to the JPA/domain packages, and the frontend discussion to the main route, store, and auth-related components. That would make the docs more useful not only during technical reviews, but also during future maintenance when someone needs to jump from an architectural concept straight into the code.
- **Formalize deployment further if the project grows beyond the current Linux host model:** The current deployment story is pragmatic and works well for the repo’s scope: Docker Compose, multi-architecture images, and prod scripts are enough to support the current host-based deployment flow. If the project starts needing repeatable team-operated releases, stronger environment parity, or more automated rollback and promotion behavior, I would move toward a more formal deployment pipeline. That could mean codifying more infrastructure assumptions, shifting more release behavior into CI/CD, and reducing the amount of manual operational knowledge currently carried in scripts and docs.

## Where To Read More

- Project overview and runtime instructions: [`../README.md`](../README.md)
- Structured architecture reference: [`architecture.md`](./architecture.md)
- ADR index: [`adr/README.md`](./adr/README.md)
- Backend-specific notes: [`../job-portal-backend/README.md`](../job-portal-backend/README.md)
- API exploration assets: [`insomnia/README.md`](./insomnia/README.md)
