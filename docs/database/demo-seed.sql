-- Demo seed data for local development and UI walkthroughs.
-- This script resets applications, jobs, and users, then inserts:
--   - 20 realistic jobs
--   - 4 users
--   - 30 applications with mixed lifecycle statuses
--
-- Local login uses database-backed users with these demo credentials:
--   - admin@local.test / admin123
--   - user@local.test / user123
--   - maya.patel@example.com / applicant123
--
-- The seeded `user` row below is intentionally named `user` so the
-- default applicant login can see seeded application history.
-- Daniel Kim is seeded as disabled so the admin Users tab can show that state.

START TRANSACTION;

DELETE FROM applications;
DELETE FROM jobs;
DELETE FROM users;

INSERT INTO users (
    id,
    name,
    email,
    password,
    auth_provider,
    role,
    enabled,
    created_at,
    updated_at
) VALUES
    (1, 'admin', 'admin@local.test', '{noop}admin123', 'LOCAL', 'ADMIN', TRUE, '2026-05-01 09:00:00', '2026-05-01 09:00:00'),
    (2, 'user', 'user@local.test', '{noop}user123', 'LOCAL', 'APPLICANT', TRUE, '2026-05-01 09:05:00', '2026-05-01 09:05:00'),
    (3, 'Maya Patel', 'maya.patel@example.com', '{noop}applicant123', 'LOCAL', 'APPLICANT', TRUE, '2026-05-01 09:10:00', '2026-05-01 09:10:00'),
    (4, 'Daniel Kim', 'daniel.kim@example.com', '{noop}applicant123', 'LOCAL', 'APPLICANT', FALSE, '2026-05-01 09:15:00', '2026-05-01 09:15:00');

INSERT INTO jobs (
    id,
    title,
    description,
    company,
    posted_date,
    created_at,
    updated_at,
    status
) VALUES
    (1, 'Senior Java Backend Developer', 'Build and maintain Spring Boot APIs for enterprise integration workloads.', 'IBM', '2026-05-02', '2026-05-02 08:30:00', '2026-05-02 08:30:00', 'OPEN'),
    (2, 'Cloud Infrastructure Engineer', 'Automate cloud networking, observability, and platform reliability across shared services.', 'Oracle', '2026-05-02', '2026-05-02 09:15:00', '2026-05-02 09:15:00', 'OPEN'),
    (3, 'AI Platform Engineer', 'Support model-serving infrastructure and internal developer tooling for applied AI products.', 'OpenAI', '2026-05-03', '2026-05-03 10:00:00', '2026-05-03 10:00:00', 'OPEN'),
    (4, 'Frontend Engineer', 'Develop responsive React interfaces for customer-facing productivity tools.', 'Google', '2026-05-03', '2026-05-03 11:20:00', '2026-05-03 11:20:00', 'OPEN'),
    (5, 'DevOps Engineer', 'Improve CI pipelines, deployment automation, and service diagnostics for cloud-hosted systems.', 'Microsoft', '2026-05-04', '2026-05-04 08:45:00', '2026-05-10 16:40:00', 'CLOSED'),
    (6, 'Site Reliability Engineer', 'Own production reliability, incident response, and service performance for distributed platforms.', 'Amazon Web Services', '2026-05-04', '2026-05-04 09:30:00', '2026-05-04 09:30:00', 'OPEN'),
    (7, 'Data Engineer', 'Design ETL pipelines and analytics-ready datasets for business reporting and internal dashboards.', 'Salesforce', '2026-05-05', '2026-05-05 10:10:00', '2026-05-05 10:10:00', 'OPEN'),
    (8, 'Product Security Engineer', 'Review application architecture, threat models, and security controls across product teams.', 'Shopify', '2026-05-05', '2026-05-05 13:00:00', '2026-05-11 17:05:00', 'CLOSED'),
    (9, 'Machine Learning Engineer', 'Deploy machine learning pipelines and optimize model inference workloads in production.', 'NVIDIA', '2026-05-06', '2026-05-06 08:55:00', '2026-05-06 08:55:00', 'OPEN'),
    (10, 'Integration Engineer', 'Build secure API integrations between internal services and third-party enterprise systems.', 'Cisco', '2026-05-06', '2026-05-06 14:20:00', '2026-05-06 14:20:00', 'OPEN'),
    (11, 'Full Stack Developer', 'Ship customer features across Java services, React frontends, and supporting cloud infrastructure.', 'Adobe', '2026-05-07', '2026-05-07 09:40:00', '2026-05-12 15:30:00', 'CLOSED'),
    (12, 'Platform Engineer', 'Create internal tooling that simplifies deployments, secrets management, and operational visibility.', 'Stripe', '2026-05-07', '2026-05-07 15:10:00', '2026-05-07 15:10:00', 'OPEN'),
    (13, 'Software Engineer, Payments', 'Develop backend services that support payment flows, ledger events, and fraud controls.', 'Uber', '2026-05-08', '2026-05-08 10:25:00', '2026-05-08 10:25:00', 'OPEN'),
    (14, 'Cloud Solutions Architect', 'Guide customers through secure migration patterns and reference architectures on cloud platforms.', 'SAP', '2026-05-08', '2026-05-08 16:45:00', '2026-05-13 12:15:00', 'CLOSED'),
    (15, 'Developer Experience Engineer', 'Improve local tooling, templates, and workflow automation for software teams.', 'GitHub', '2026-05-09', '2026-05-09 09:05:00', '2026-05-09 09:05:00', 'OPEN'),
    (16, 'QA Automation Engineer', 'Expand automated regression coverage for APIs, UI flows, and release smoke tests.', 'Intel', '2026-05-09', '2026-05-09 14:35:00', '2026-05-09 14:35:00', 'OPEN'),
    (17, 'Solutions Consultant', 'Support technical discovery sessions and implementation planning for enterprise customers.', 'ServiceNow', '2026-05-10', '2026-05-10 11:15:00', '2026-05-14 10:05:00', 'CLOSED'),
    (18, 'Analytics Engineer', 'Model warehouse data and publish trusted metrics for operational and product reporting.', 'LinkedIn', '2026-05-10', '2026-05-10 15:45:00', '2026-05-10 15:45:00', 'OPEN'),
    (19, 'Mobile Platform Engineer', 'Maintain shared SDKs, release tooling, and app performance standards across mobile teams.', 'Airbnb', '2026-05-11', '2026-05-11 10:50:00', '2026-05-11 10:50:00', 'OPEN'),
    (20, 'API Support Engineer', 'Investigate integration issues, reproduce defects, and partner with engineering on fixes.', 'Atlassian', '2026-05-11', '2026-05-11 16:20:00', '2026-05-15 09:25:00', 'CLOSED');

INSERT INTO applications (
    id,
    user_id,
    job_id,
    status,
    created_at,
    updated_at
) VALUES
    (1, 2, 1, 'APPLIED', '2026-05-12 09:00:00', '2026-05-12 09:00:00'),
    (2, 2, 2, 'REVIEWING', '2026-05-12 09:10:00', '2026-05-14 11:30:00'),
    (3, 2, 3, 'ACCEPTED', '2026-05-12 09:20:00', '2026-05-18 16:45:00'),
    (4, 2, 5, 'REJECTED', '2026-05-12 09:30:00', '2026-05-16 14:05:00'),
    (5, 2, 7, 'WITHDRAWN', '2026-05-12 09:40:00', '2026-05-13 13:20:00'),
    (6, 2, 9, 'APPLIED', '2026-05-12 09:50:00', '2026-05-12 09:50:00'),
    (7, 2, 11, 'REVIEWING', '2026-05-12 10:00:00', '2026-05-15 10:15:00'),
    (8, 2, 13, 'ACCEPTED', '2026-05-12 10:10:00', '2026-05-19 09:35:00'),
    (9, 2, 15, 'APPLIED', '2026-05-12 10:20:00', '2026-05-12 10:20:00'),
    (10, 2, 17, 'REJECTED', '2026-05-12 10:30:00', '2026-05-17 15:40:00'),
    (11, 3, 1, 'REVIEWING', '2026-05-12 11:00:00', '2026-05-15 09:45:00'),
    (12, 3, 4, 'APPLIED', '2026-05-12 11:10:00', '2026-05-12 11:10:00'),
    (13, 3, 6, 'ACCEPTED', '2026-05-12 11:20:00', '2026-05-20 10:00:00'),
    (14, 3, 8, 'REJECTED', '2026-05-12 11:30:00', '2026-05-16 16:30:00'),
    (15, 3, 10, 'APPLIED', '2026-05-12 11:40:00', '2026-05-12 11:40:00'),
    (16, 3, 12, 'WITHDRAWN', '2026-05-12 11:50:00', '2026-05-14 08:20:00'),
    (17, 3, 14, 'REVIEWING', '2026-05-12 12:00:00', '2026-05-15 14:10:00'),
    (18, 3, 16, 'ACCEPTED', '2026-05-12 12:10:00', '2026-05-21 09:05:00'),
    (19, 3, 18, 'APPLIED', '2026-05-12 12:20:00', '2026-05-12 12:20:00'),
    (20, 3, 20, 'REJECTED', '2026-05-12 12:30:00', '2026-05-18 11:55:00'),
    (21, 4, 2, 'APPLIED', '2026-05-12 13:00:00', '2026-05-12 13:00:00'),
    (22, 4, 3, 'REVIEWING', '2026-05-12 13:10:00', '2026-05-14 10:40:00'),
    (23, 4, 4, 'ACCEPTED', '2026-05-12 13:20:00', '2026-05-19 13:25:00'),
    (24, 4, 6, 'APPLIED', '2026-05-12 13:30:00', '2026-05-12 13:30:00'),
    (25, 4, 7, 'REVIEWING', '2026-05-12 13:40:00', '2026-05-15 16:10:00'),
    (26, 4, 8, 'WITHDRAWN', '2026-05-12 13:50:00', '2026-05-13 12:00:00'),
    (27, 4, 10, 'REJECTED', '2026-05-12 14:00:00', '2026-05-18 09:20:00'),
    (28, 4, 12, 'APPLIED', '2026-05-12 14:10:00', '2026-05-12 14:10:00'),
    (29, 4, 19, 'ACCEPTED', '2026-05-12 14:20:00', '2026-05-20 15:30:00'),
    (30, 4, 20, 'REVIEWING', '2026-05-12 14:30:00', '2026-05-17 10:25:00');

ALTER TABLE users AUTO_INCREMENT = 5;
ALTER TABLE jobs AUTO_INCREMENT = 21;
ALTER TABLE applications AUTO_INCREMENT = 31;

COMMIT;
