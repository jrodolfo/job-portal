# ADR 0003: Use MySQL as the Primary Application Database

- Status: `accepted`
- Date: `2026-05-25`

## Context

The backend persists application data through Spring Data JPA and expects a
relational database in both local and EC2-style runtime paths.

The repository already centers local runtime around a MySQL container and uses
MySQL-specific configuration in the backend.

## Decision

Use MySQL as the primary runtime database for the application.

Use H2 and Testcontainers only for test support where appropriate, not as the
main runtime persistence model.

## Rationale

MySQL is already the concrete operational assumption in:

- `job-portal-backend/src/main/resources/application.yml`
- `docker-compose.yml`
- the local `.env` contract
- the deployment and smoke-test documentation

Keeping MySQL as the primary target keeps local, Docker, and EC2 stories
aligned instead of pretending the app is database-agnostic when the runtime
path is not.

## Consequences

The persistence story is clear and consistent across documentation and runtime
automation.

Developers can reproduce production-like behavior locally through Docker
Compose.

The downside is tighter coupling to MySQL dialect behavior, configuration, and
container availability during full-stack local runs.

## Revisit Triggers

Reconsider this decision if a managed cloud database, another relational
engine, or a stronger migration strategy changes the real operational target.
