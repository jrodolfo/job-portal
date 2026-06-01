# Job Portal Backend

This module is the Spring Boot backend for the `Job Portal` repository.

Backend namespace reference:

- Maven coordinates: `net.jrodolfo:jobportal`
- Java base package: `net.jrodolfo.jobportal`

OpenTelemetry runtime:

- The backend Docker image includes the OpenTelemetry Java Agent.
- OTLP export is configured via environment variables (see root `.env` and `docker-compose*.yml`).

Schema management:

- Flyway owns schema changes through versioned migrations.
- Hibernate runs in validation mode so startup still checks entity/schema alignment.
- The current baseline and follow-up migrations live under `src/main/resources/db/migration/`.

Current backend behavior:

- Applicants can create applications, withdraw them, and reapply after a withdrawn state.
- Public `GET /api/jobs` returns only `OPEN` jobs.
- Admins use the authenticated full jobs view and can move jobs between `OPEN` and `CLOSED`.
- Applying to a closed job is rejected by the backend.
- Admins can review application statuses such as `REVIEWING`, `ACCEPTED`, and `REJECTED`.
- Admins can list users, create applicant users, edit applicant name/email, and enable or disable applicant users.
- Admin users are protected from this management flow: they cannot be created, edited, deleted, or promoted through the admin user endpoints.
- Jobs with existing applications cannot be deleted; the backend returns a clear `409 Conflict` instead.
- Application response payloads can include `createdAt` and `updatedAt`, which the frontend uses for `Applied On` and `Last Updated` display.
- Local email/password authentication is database-backed. The local bootstrap creates the default `admin@local.test` and `user@local.test` accounts when they are missing.

Local demo bootstrap:

- For a full local demo with seeded jobs, users, and applications, use the root helper:

```bash
bash scripts/local/start-with-demo-data.sh
```

- That flow lives at the repository root because it starts the full stack and loads demo data into MySQL.

Tests:

```bash
mvn test
```

Local Java requirement:

- Use Java 21 for local Maven work on this module.
- If your shell defaults to another JDK, run Maven like this:

```bash
export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-21.jdk/Contents/Home
export PATH="$JAVA_HOME/bin:$PATH"
mvn test
```

If you are running the local stack through Docker and only backend code changed, rebuild just the backend service from the repository root:

```bash
docker compose up -d --build backend
```

Related repository docs:

- Root project overview: [../README.md](../README.md)
- Architecture reference: [../docs/architecture.md](../docs/architecture.md)
- Architecture walkthrough: [../docs/architecture-walkthrough.md](../docs/architecture-walkthrough.md)
- Architecture Decision Records: [../docs/adr/README.md](../docs/adr/README.md)
