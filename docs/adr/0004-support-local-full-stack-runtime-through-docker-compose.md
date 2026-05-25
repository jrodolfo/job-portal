# ADR 0004: Support Local Full-Stack Runtime Through Docker Compose

- Status: `accepted`
- Date: `2026-05-25`

## Context

The repository contains a frontend, backend, database, observability
components, and image build assets. Running these pieces separately is possible
but increases setup friction and environment drift.

The project also needs a reproducible way to demonstrate the full stack during
maintenance, onboarding, and interviews.

## Decision

Use Docker Compose as the primary local full-stack runtime path for the
repository.

The compose stack includes at least:

- MySQL
- the Spring Boot backend
- the React frontend
- the OpenTelemetry collector
- Jaeger for local tracing visualization

## Rationale

This project is easier to operate and explain when the default path is one
command from the repo root.

Compose also matches the repository structure well because root-level files can
orchestrate both application modules and the supporting operational services.

## Consequences

Onboarding and smoke testing become simpler because the stack can be started
with a single root command.

Observability becomes part of the default local runtime rather than an afterthought.

The tradeoff is that Compose becomes a central piece of the developer
experience, so path mistakes or service misconfiguration have broader impact.

## Revisit Triggers

Reconsider this decision if local development moves to cloud-hosted services,
if Kubernetes becomes the primary development target, or if frontend/backend
development is intentionally split for speed over full-stack fidelity.
