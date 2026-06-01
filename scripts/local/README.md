# Local Scripts

These scripts operate the local Docker-based development environment.

Run them from the repository root:

```bash
bash scripts/local/start.sh
```

Or from this directory:

```bash
./start.sh
```

The shell scripts resolve the repository root from their own path, so both styles work.

## Scripts

| Script | Purpose |
| --- | --- |
| `start.sh` | Builds and starts the local Docker Compose stack. |
| `start-with-demo-data.sh` | Starts the local stack, loads demo seed data, and prints URLs plus local credentials. |
| `seed-demo-data.sh` | Reloads the demo seed data into an already running local MySQL container. |
| `stop.sh` | Stops and removes the local Docker Compose containers and network. |
| `upload-docker-images.sh` | Builds and pushes the configured Docker images with Docker Buildx Bake. |

## Common Flows

Start the local stack without changing database contents:

```bash
bash scripts/local/start.sh
```

Start a demo-ready local stack with seeded jobs, users, and applications:

```bash
bash scripts/local/start-with-demo-data.sh
```

Reload only the demo seed data after the stack is already running:

```bash
bash scripts/local/seed-demo-data.sh
```

Stop the local stack:

```bash
bash scripts/local/stop.sh
```

Reset all local Docker database data:

```bash
docker compose down -v
```

## Notes

- `start-with-demo-data.sh` calls `start.sh` first, then retries `seed-demo-data.sh` while MySQL is starting.
- `seed-demo-data.sh` resets the `jobs`, `users`, and `applications` tables using `docs/database/demo-seed.sql`.
- `upload-docker-images.sh` expects `docker-bake.hcl` at the repository root and requires Docker registry access.
