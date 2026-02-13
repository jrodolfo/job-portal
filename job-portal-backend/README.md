# Job Portal Backend

Backend namespace reference:

- Maven coordinates: `net.jrodolfo:jobportal`
- Java base package: `net.jrodolfo.jobportal`

OpenTelemetry runtime:

- The backend Docker image includes the OpenTelemetry Java Agent.
- OTLP export is configured via environment variables (see root `.env` and `docker-compose*.yml`).
