# ADR 0009: Use Runtime ddl-auto Update for Now

- Status: `accepted`
- Date: `2026-05-25`

## Context

The backend currently manages schema changes through Hibernate configuration
instead of a formal migration tool.

This repository is still optimized for fast local setup, demoability, and
practical evolution from a course-derived foundation into a more production-ready
project. At the same time, the current persistence model already has real
runtime expectations around MySQL and stable application behavior.

## Decision

Keep `spring.jpa.hibernate.ddl-auto=update` as the active schema-management
strategy for now.

Do not introduce Flyway or Liquibase yet.

## Rationale

This choice keeps the project easy to run and evolve while the schema is still
relatively lightweight and the main goal is maintainable full-stack operation
rather than formal database release management.

It also aligns with the current local development story, where Docker Compose
and application startup should be able to bootstrap a working environment with
minimal manual setup.

## Consequences

Schema changes remain easy to apply during local development and iterative
maintenance.

The project avoids adding migration tooling, migration ordering, and migration
review overhead before the schema lifecycle clearly requires it.

The downside is reduced control over explicit schema history, less disciplined
change tracking, and more risk if the application starts needing stricter
production-grade database evolution practices.

## Revisit Triggers

Reconsider this decision if the schema becomes more complex, if multiple
environments require tightly controlled rollout history, if data migrations
become common, or if deployment safety requires explicit versioned migrations.
