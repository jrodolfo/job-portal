# Job Portal Frontend

This module is the React frontend for the `Job Portal` repository.

It provides the browser UI for:

- login and OAuth-related flows
- applicant job application flows
- admin job and application management
- navigation and page composition
- client-side state management
- HTTP calls to the Spring Boot backend

## Current Frontend Workflows

Applicants can:

- log in with local credentials or OAuth
- browse open jobs only
- apply, withdraw, and reapply
- see application status, `Applied On`, and `Last Updated` when available

Admins can:

- use overview cards for total jobs, open jobs, closed jobs, and total applications
- manage the dashboard through `Jobs`, `Add Job`, and `Applications` tabs
- create, edit, close, reopen, and delete jobs
- review applications, update statuses, and use grouped/filterable application views

Current UI behavior:

- Closed jobs disappear from the applicant dashboard instead of remaining available for new applications.
- Jobs with existing applications cannot be deleted and should be closed instead.
- The admin applications tab shows status summaries plus `Applied On` and `Last Updated` when those timestamps are meaningful.

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

### Testing Layers

The frontend currently uses three testing layers:

- Unit/component tests with `Vitest` and `Testing Library` for focused component behavior and local state changes
- MSW-backed integration tests for realistic request/response flows without needing the real backend
- Playwright E2E tests for full browser workflows across the running stack

The MSW layer fills the gap between isolated mocked component tests and full browser E2E coverage. It exercises real network-shaped UI behavior and helps catch API/UI contract drift earlier.

Current test locations:

- component tests: `src/components/*.test.jsx`
- integration tests: `src/components/*.integration.test.jsx`
- browser tests: `tests/e2e/*.spec.js`

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

If you want the fuller seeded demo environment first, use:

```bash
bash scripts/local/start-with-demo-data.sh
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
