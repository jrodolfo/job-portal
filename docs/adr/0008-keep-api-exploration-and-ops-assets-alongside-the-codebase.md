# ADR 0008: Keep API Exploration and Ops Assets Alongside the Codebase

- Status: `accepted`
- Date: `2026-05-25`

## Context

This repository includes more than application code. It also includes files for:

- Insomnia-based API exploration
- OpenTelemetry collector configuration
- database query helpers
- Docker-related notes and commands
- developer operational notes

These artifacts are useful during maintenance, demos, and onboarding.

## Decision

Keep these operational and API exploration assets under `docs/` alongside the
codebase rather than moving them to a separate repo or treating them as private
one-off notes.

## Rationale

The repository is intended to be runnable, explainable, and demoable.

Keeping these assets close to the implementation makes it easier to validate
the system, reproduce workflows, and discuss operational tradeoffs in an
interview or maintenance context.

## Consequences

The repo becomes a more complete source of truth for how the system is used and
operated.

New contributors can discover test flows, observability config, and helper
artifacts without leaving the repository.

The downside is that documentation drift becomes a real maintenance concern if
these assets are not updated together with the code.

## Revisit Triggers

Reconsider this decision if the project adopts a centralized documentation
platform, if sensitive operational material must be separated, or if these
assets become too large or environment-specific for the main repo.
