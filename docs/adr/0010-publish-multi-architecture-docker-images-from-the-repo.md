# ADR 0010: Publish Multi-Architecture Docker Images from the Repo

- Status: `accepted`
- Date: `2026-05-25`

## Context

This repository is developed on machines that may not match the deployment
architecture of the target environment.

The project documentation already assumes a practical path where development
happens on Apple Silicon while deployment may happen on Linux AMD64 hosts such
as EC2. Both the frontend and backend images therefore need a consistent image
publication story that does not depend on building only for the maintainer's
local CPU architecture.

## Decision

Build and publish multi-architecture Docker images for both application
modules from this repository.

The supported target platforms are:

- `linux/amd64`
- `linux/arm64`

## Rationale

This keeps local development hardware and deployment hardware decoupled.

The repo already has the right structure for this approach:

- `docker-bake.hcl` defines both module targets
- both targets declare `linux/amd64` and `linux/arm64`
- `scripts/local/upload-docker-images.sh` publishes through `docker buildx bake`

That makes multi-arch publishing part of the documented operational workflow
rather than an ad hoc release detail.

## Consequences

Developers can build on ARM64 machines without producing images that fail on
AMD64 deployment targets.

The deployment story becomes more credible and less fragile for EC2-style use.

The downside is a more complex image build path, slower publish operations than
single-architecture images, and stronger dependency on `buildx` tooling.

## Revisit Triggers

Reconsider this decision if deployment standardizes on one architecture, if
image builds move fully into CI/CD with different publish semantics, or if a
registry/platform constraint changes the supported target list.
