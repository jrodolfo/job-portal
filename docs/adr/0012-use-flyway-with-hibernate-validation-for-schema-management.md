# ADR 0012: Use Flyway with Hibernate Validation for Schema Management

- Status: `accepted`
- Date: `2026-05-25`

## Context

The backend originally relied on `spring.jpa.hibernate.ddl-auto=update` to
mutate the schema during startup.

That approach kept local setup simple, but it also made schema evolution less
explicit. Once the project gained a more complete architecture and a stronger
documentation story, the database lifecycle became important enough to treat as
versioned change history instead of implicit runtime behavior.

The repository also needs a migration path that does not force developers to
drop their local databases just to adopt the new approach.

## Decision

Use Flyway as the active schema-management tool for the backend.

Keep Hibernate in `validate` mode so application startup still verifies that
the entity model matches the migrated schema.

Start with a compatibility-oriented baseline migration that can create the
expected schema on an empty database and reconcile common legacy column shapes
from earlier local environments.

## Rationale

Flyway gives the project explicit, reviewable, versioned schema changes.

Hibernate validation preserves a useful safety check: the application should
still fail fast if the runtime mapping no longer matches the database that
Flyway produced.

Using a compatibility-oriented baseline reduces migration friction for a repo
that has already been used locally with Hibernate-managed schema updates. That
lets the project improve its discipline without requiring a destructive reset
of every existing development database.

## Consequences

Schema evolution is now part of the repository’s versioned change history
instead of a side effect of application startup.

Database changes now require migration maintenance, review, and ordering
discipline.

The backend gains a safer path for future rollout control, but it also becomes
more important to keep entity changes and migrations aligned in the same
change set.

The initial baseline migration is broader than a typical greenfield migration
because it also accounts for compatibility with legacy local schemas. Future
migrations should stay smaller and more incremental.

## Revisit Triggers

Reconsider this decision if the project needs richer data-migration workflows,
cross-database support beyond the current model, or a change-management process
that would be better served by a different migration system.
