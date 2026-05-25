# ADR 0007: Keep Local and Prod Operations in Separate Script Paths

- Status: `accepted`
- Date: `2026-05-25`

## Context

This repository supports two materially different operational environments:

- local development on a workstation
- production-oriented startup and shutdown on an EC2-style Linux host

Those environments do not have the same assumptions around compose files,
required environment variables, and operator workflows.

## Decision

Keep local and prod operations in separate script paths:

- `scripts/local/*`
- `scripts/prod/*`

The prod scripts explicitly validate compose availability, runtime prerequisites,
and required OpenTelemetry export variables.

## Rationale

Separating the script paths keeps environment-specific behavior explicit rather
than hidden behind one ambiguous command.

That is a better fit for this repo than pretending local convenience and
prod-oriented safety checks are the same workflow.

## Consequences

The operational story is clearer and easier to reason about.

Prod scripts can enforce stricter checks without making local iteration harder.

The tradeoff is some duplication and the need to keep local/prod behavior in
sync where appropriate.

## Revisit Triggers

Reconsider this decision if deployment becomes fully CI/CD-managed, if the
local and prod workflows converge substantially, or if a single higher-level
orchestration entrypoint replaces these scripts.
