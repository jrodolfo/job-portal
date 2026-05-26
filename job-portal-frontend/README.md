# Job Portal Frontend

This module is the React frontend for the `Job Portal` repository.

It provides the browser UI for:

- login and OAuth-related flows
- applicant job application flows
- admin job and application management
- navigation and page composition
- client-side state management
- HTTP calls to the Spring Boot backend

Current supported workflows:

- Applicants can log in, apply to jobs, withdraw applications, and reapply later.
- Admins can create, edit, and delete jobs.
- Admins can review submitted applications and update their statuses.

## Tech Stack

- React 19
- Vite 6
- Redux Toolkit
- React Router
- Axios
- Bootstrap 5
- Vitest + Testing Library

## Run the Frontend Independently

From the repository root:

```bash
cd job-portal-frontend
npm install
npm run dev
```

The dev server runs on:

- `http://localhost:5173`

By default, the frontend expects the backend to be available at the URL defined
in:

- `src/config/backend.js`

In the normal full-stack workflow, the frontend is usually run together with
the backend, MySQL, and observability services through the root
`docker-compose.yml`.

If you are iterating on the Dockerized frontend instead of `npm run dev`, rebuild just the frontend service from the repository root:

```bash
docker compose up -d --build frontend
```

## Tests

Run the frontend test suite with:

```bash
npm test
```

Run the Playwright browser suite with:

```bash
npm run test:e2e
```

The browser suite assumes the local stack is already running, typically through:

```bash
bash scripts/local/start.sh
```

Or run coverage with:

```bash
npm run test:coverage
```

## Related Repository Docs

- Root project overview: [../README.md](../README.md)
- Architecture reference: [../docs/architecture.md](../docs/architecture.md)
- Architecture walkthrough: [../docs/architecture-walkthrough.md](../docs/architecture-walkthrough.md)
- Architecture Decision Records: [../docs/adr/README.md](../docs/adr/README.md)
