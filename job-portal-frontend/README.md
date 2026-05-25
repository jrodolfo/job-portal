# Job Portal Frontend

This module is the React frontend for the `Job Portal` repository.

It provides the browser UI for:

- login and OAuth-related flows
- applicant and admin interactions
- navigation and page composition
- client-side state management
- HTTP calls to the Spring Boot backend

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

## Tests

Run the frontend test suite with:

```bash
npm test
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
