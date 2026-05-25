# ADR 0006: Use OpenTelemetry Collector-Based Observability for Local and EC2

- Status: `accepted`
- Date: `2026-05-25`

## Context

The project needs a practical observability story that works locally and in EC2
deployment scenarios without hard-coding a single vendor into the application
binary.

The repository already includes collector config files, backend environment
variables, and local Jaeger integration.

## Decision

Use OpenTelemetry with a collector-centered design:

- the backend image includes Java auto-instrumentation support
- local runs export to an OpenTelemetry collector and view traces in Jaeger
- prod-oriented runs still export through a collector, which can forward to an
  upstream telemetry provider

## Rationale

This keeps instrumentation behavior in the app while keeping export routing and
vendor-facing details in deployable config.

That separation is a strong fit for a repo that supports both local
observability demos and EC2-style deployment with environment-driven upstream
configuration.

## Consequences

Tracing becomes a first-class architectural concern rather than a hidden add-on.

The project gains a cleaner migration path between local tracing and hosted
observability backends.

The downside is more moving parts in the compose stack and more environment
variables to keep consistent across local and prod paths.

## Revisit Triggers

Reconsider this decision if tracing is moved into a platform-managed solution,
if logs/metrics/traces need a very different deployment model, or if the app
stops benefiting from collector indirection.
