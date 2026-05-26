# Architecture Decision Records

This folder stores Architecture Decision Records (ADRs) for `Job Portal`.

ADRs capture the architectural decisions that shaped the repository, why those
decisions were made, and what tradeoffs they introduced. They complement
[`docs/architecture.md`](../architecture.md) and
[`docs/architecture-walkthrough.md`](../architecture-walkthrough.md), which
describe the current system design in structured and interview-friendly forms,
by preserving the decision history behind that design.

## Why This Repo Uses ADRs

`Job Portal` has several decisions that are easier to maintain when the
reasoning is written down explicitly, for example:

- why the frontend and backend remain separate modules
- why MySQL remains the primary application database
- why local full-stack development is centered on Docker Compose
- why Google OAuth2 is combined with local auth and JWT issuance
- why OpenTelemetry uses a collector-centered setup for local and EC2 runs
- why deployment and API exploration assets stay alongside the repository
- why schema management now uses Flyway migrations plus Hibernate validation

These records are intentionally short enough to scan quickly during onboarding,
maintenance, or interview preparation.

## Status Values

- `proposed`: decision is being discussed and is not yet the project baseline
- `accepted`: decision is in effect and reflected in the codebase
- `superseded`: decision was replaced by a newer ADR

## When To Add A New ADR

Add an ADR when a change introduces or revises a durable architectural
decision, for example:

- module boundaries
- runtime and deployment model choices
- storage model changes
- authentication boundary changes
- observability model changes
- documentation source-of-truth decisions

Do not add an ADR for ordinary implementation work such as:

- UI wording polish
- styling changes
- routine bug fixes
- isolated test additions without a new design decision

## Naming Convention

Use zero-padded numeric prefixes and kebab-case titles:

```text
0001-short-kebab-case-title.md
```

Each ADR should focus on one decision.

## Template

Start new records from [template.md](template.md).

## Current ADRs

- [0001-use-a-two-module-job-portal-workspace.md](0001-use-a-two-module-job-portal-workspace.md)
- [0002-keep-react-frontend-separate-from-spring-boot-backend.md](0002-keep-react-frontend-separate-from-spring-boot-backend.md)
- [0003-use-mysql-as-the-primary-application-database.md](0003-use-mysql-as-the-primary-application-database.md)
- [0004-support-local-full-stack-runtime-through-docker-compose.md](0004-support-local-full-stack-runtime-through-docker-compose.md)
- [0005-use-google-oauth2-alongside-local-auth-and-jwt.md](0005-use-google-oauth2-alongside-local-auth-and-jwt.md)
- [0006-use-opentelemetry-collector-based-observability-for-local-and-ec2.md](0006-use-opentelemetry-collector-based-observability-for-local-and-ec2.md)
- [0007-keep-local-and-prod-operations-in-separate-script-paths.md](0007-keep-local-and-prod-operations-in-separate-script-paths.md)
- [0008-keep-api-exploration-and-ops-assets-alongside-the-codebase.md](0008-keep-api-exploration-and-ops-assets-alongside-the-codebase.md)
- [0009-use-runtime-ddl-auto-update-for-now.md](0009-use-runtime-ddl-auto-update-for-now.md)
- [0010-publish-multi-architecture-docker-images-from-the-repo.md](0010-publish-multi-architecture-docker-images-from-the-repo.md)
- [0011-keep-the-backend-rest-openapi-first-and-reusable.md](0011-keep-the-backend-rest-openapi-first-and-reusable.md)
- [0012-use-flyway-with-hibernate-validation-for-schema-management.md](0012-use-flyway-with-hibernate-validation-for-schema-management.md)
- [0013-define-job-and-application-lifecycle-rules.md](0013-define-job-and-application-lifecycle-rules.md)
