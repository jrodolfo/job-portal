# ADR 0001: Use a Two-Module Job Portal Workspace

- Status: `accepted`
- Date: `2026-05-25`

## Context

This repository needs to support a browser UI and a Java backend while keeping
their build tools, dependency graphs, and release concerns manageable.

The project is no longer just a course exercise. It now includes Docker-based
local runtime, EC2 deployment concerns, observability files, API exploration
artifacts, and separate frontend/backend workflows.

## Decision

Keep the repository as a two-module workspace:

- `job-portal-backend` for the Spring Boot API
- `job-portal-frontend` for the React application

Shared operational assets remain at the repository root in files such as
`docker-compose.yml`, `docker-compose.prod.yml`, `scripts/`, and `docs/`.

## Rationale

This layout matches the actual runtime split of the system while keeping
everything needed to run the stack together in one repository.

It avoids forcing Java and JavaScript tooling into a single build system, and
it keeps root-level infrastructure files in a place where they can orchestrate
both modules without either module pretending to own the whole stack.

## Consequences

The project is easier to explain because the module split mirrors the deployed
shape of the system.

Backend and frontend can evolve with their own dependencies, scripts, and test
commands.

The downside is that some operational commands must coordinate paths across the
repo root and both modules.

## Revisit Triggers

Reconsider this decision if the frontend is replaced by server-rendered pages,
if the backend is split into multiple services, or if the repo grows enough to
justify a larger monorepo build strategy.
