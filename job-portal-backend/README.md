# Job Portal Backend

This module is the Spring Boot backend for the `Job Portal` repository.

Backend namespace reference:

- Maven coordinates: `net.jrodolfo:jobportal`
- Java base package: `net.jrodolfo.jobportal`

OpenTelemetry runtime:

- The backend Docker image includes the OpenTelemetry Java Agent.
- OTLP export is configured via environment variables (see root `.env` and `docker-compose*.yml`).

Related repository docs:

- Root project overview: [../README.md](../README.md)
- Architecture reference: [../docs/architecture.md](../docs/architecture.md)
- Architecture walkthrough: [../docs/architecture-walkthrough.md](../docs/architecture-walkthrough.md)
- Architecture Decision Records: [../docs/adr/README.md](../docs/adr/README.md)
