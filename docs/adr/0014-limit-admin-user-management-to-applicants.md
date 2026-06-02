# ADR 0014: Limit Admin User Management To Applicants

- Status: `accepted`
- Date: `2026-06-01`

## Context

The admin dashboard needs basic user management, but unrestricted user CRUD
would create unnecessary authorization complexity. In particular, admin account
creation, admin promotion, and admin deletion require clearer governance than
this practice project needs.

The project also uses local email/password authentication for development
and demo flows. Applicant users can create their own accounts through public
registration, so the admin dashboard does not need to create or edit applicant
identity fields.

## Decision

Admin user management is limited to applicant users.

Admins can:

- list users through a safe admin response DTO
- enable or disable applicant users

Admins cannot:

- create applicant users from the admin dashboard
- create admin users
- edit applicant name or email
- edit admin users
- delete users
- promote applicants to admins
- disable admin users from the dashboard

Local email/password authentication is database-backed. The application
bootstraps the default local `admin` and `user` accounts when they are missing.

## Rationale

This gives the admin persona useful access-control functionality without
duplicating the public registration flow or introducing a Super Admin model,
role escalation rules, or admin deletion edge cases. It also keeps the demo
realistic: users shown in the database and users used for local login are the
same kind of records.

Using a safe DTO for admin user lists avoids exposing password hashes to the
frontend while still giving the UI the fields it needs.

## Consequences

The first version is intentionally narrow and easier to reason about. Applicant
access can be disabled without deleting records that may be connected to
applications.

Admin lifecycle management remains outside the dashboard. If the project later
needs multiple admin operators, it will need a separate decision for how admin
creation, promotion, demotion, and deletion should work.

## Revisit Triggers

Revisit this decision if the project needs:

- multiple administrators managed from the UI
- role promotion or demotion workflows
- audit logs for account changes
- password reset flows
- a Super Admin or owner role
