# ADR 0013: Define Job and Application Lifecycle Rules

- Status: `accepted`
- Date: `2026-05-26`

## Context

The repository now supports a fuller end-to-end job and application workflow
than the original course-derived baseline.

Applicants can browse jobs, submit applications, withdraw them, and later
reapply. Admins can create and manage jobs, review application statuses, and
operate the system through the same backend and frontend stack that local and
Docker-based development already use.

As these flows became more complete, a few rules stopped being incidental
implementation details and became durable product behavior:

- whether a withdrawn application should permanently block reapply
- whether admins should have explicit review states beyond `APPLIED`
- whether a job with submitted applications can still be hard-deleted

Those rules affect both the Spring Boot backend and the React frontend, so they
should be documented as a shared lifecycle decision instead of being inferred
from scattered code paths.

## Decision

Use the following lifecycle rules in the repository:

- applicants may apply to a job, withdraw that application, and later reapply
- admin review operates through explicit statuses such as `REVIEWING`,
  `ACCEPTED`, and `REJECTED`
- `WITHDRAWN` remains a meaningful state in application history rather than
  deleting the application record
- jobs with existing applications cannot be deleted; the backend returns a
  clear `409 Conflict` instead

This lifecycle is implemented across:

- `job-portal-backend/src/main/java/net/jrodolfo/jobportal/service/ApplicationService.java`
- `job-portal-backend/src/main/java/net/jrodolfo/jobportal/service/JobService.java`
- `job-portal-frontend/src/components/ApplicantDashboard.jsx`
- `job-portal-frontend/src/components/AdminDashboard.jsx`

## Rationale

Allowing reapply after withdrawal matches a more realistic job-portal workflow
than treating any prior application as a permanent conflict. It also lets the
frontend expose `WITHDRAWN` as a real user-visible state instead of a dead end.

Explicit admin review states make the application lifecycle more useful than a
binary applied/not-applied model. That supports a meaningful admin dashboard
without requiring a separate workflow engine.

Blocking job deletion once applications exist preserves application history and
avoids ambiguous data-loss behavior. Returning a clear conflict response is
safer and more honest than allowing hard delete or surfacing a generic server
error.

Keeping these rules consistent across backend services and frontend dashboards
makes the project easier to maintain, test, and explain.

## Consequences

The repository now has a clearer and more realistic application lifecycle.

Applicants see explicit lifecycle behavior in the UI, including withdrawal and
reapply.

Admins can review application progress through explicit statuses rather than
only viewing raw submissions.

Job deletion becomes more constrained, which avoids accidental data loss but
also means the project will eventually need a richer archive/close/soft-delete
story if job lifecycle management grows.

The backend and frontend must stay aligned on the meaning of statuses such as
`APPLIED`, `WITHDRAWN`, `REVIEWING`, `ACCEPTED`, and `REJECTED`.

## Revisit Triggers

Reconsider this decision if:

- the project needs closed or archived jobs instead of simple delete blocking
- application history requires audit trails or richer reviewer metadata
- applicants need more granular post-review behavior than the current states
- the repository introduces pagination or more advanced workflow/reporting
  features that would benefit from a different lifecycle model
