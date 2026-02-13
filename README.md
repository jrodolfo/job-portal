# Job Portal - Backend and Frontend

A good portion of this code came from the Pluralsight online course:

**Full-stack Java Development with Spring Boot 3 and React**
by Imtiyaz Hirani

[Course Link](https://app.pluralsight.com/ilx/video-courses/full-stack-java-development-spring-boot-3-react)

I made several changes, fixed bugs, added new files, documentation etc. to help me to
run a clean code and deploy it at my localhost and at AWS.

Backend namespace reference:
- Maven coordinates: `net.jrodolfo:jobportal`
- Java base package: `net.jrodolfo.jobportal`

**Rod Oliveira** | Software Developer | [jrodolfo.net](https://jrodolfo.net) | Halifax, Canada 🍁

---

### A. Running the Application with Docker Compose

This is the easiest way to run the entire stack (Database, Backend, and Frontend) with a single command.

#### 1. Configure Environment Variables
Ensure you have a `.env` file in the root directory (see **Section C** below) with the necessary credentials, especially for Google OAuth2 if you plan to use it.

#### 2. Start the entire stack
To build and start all services (Database, Backend, and Frontend):

```bash
docker compose up --build
```

- **Frontend**: Accessible at [http://localhost:5173](http://localhost:5173)
- **Backend API**: Accessible at [http://localhost:8080](http://localhost:8080)
- **Database**: Accessible at `localhost:3307`

#### 3. Start specific services
If you only want to run part of the stack:

- **Only the Database**:
  ```bash
  docker compose up -d db
  ```
- **Database and Backend**:
  ```bash
  docker compose up --build db backend
  ```

#### 4. Stop the application
To stop and remove the containers:

```bash
docker compose down
```

#### 5. OpenTelemetry (Local)
The Docker setup includes OpenTelemetry Java auto-instrumentation for the backend and an OpenTelemetry Collector.

- Collector OTLP endpoints:
  - gRPC: `http://localhost:4317`
  - HTTP: `http://localhost:4318`
- Collector health: `http://localhost:13133`
- Jaeger UI: [http://localhost:16686](http://localhost:16686)

Use this command (same as standard local stack):

```bash
docker compose up --build
```

#### 6. OpenTelemetry (EC2 / Prod)
Use the prod override file, which:
- switches collector config to `doc/otel/collector-prod.yaml`
- keeps backend tracing enabled
- sets a lower default trace sampling (`OTEL_TRACES_SAMPLER_ARG=0.1`)
- disables local Jaeger by default

Required env var for prod collector export:
- `OTEL_UPSTREAM_OTLP_ENDPOINT` (for example, your observability vendor OTLP endpoint)
- optional: `OTEL_UPSTREAM_AUTH_HEADER` (for example, `Bearer <token>`)

```bash
docker compose -f docker-compose.yml -f docker-compose.prod.yml up -d --build
```

#### 7. OpenTelemetry Smoke Test (Local)
After starting with `docker compose up --build`, run:

```bash
curl -i http://localhost:8080/api/jobs
curl -i -u user:user123 -X POST http://localhost:8080/api/auth/login
curl -i -u admin:admin123 -H "Content-Type: application/json" \
  -d '{"title":"OTel Test","description":"trace smoke test","company":"Local"}' \
  http://localhost:8080/api/jobs
```

Then verify in Jaeger (`http://localhost:16686`):

1. Service `job-portal-backend` appears in the service list.
2. New traces exist for `GET /api/jobs`, `POST /api/auth/login`, and `POST /api/jobs`.
3. At least one trace includes child spans (for example Spring MVC/security/database work).
4. Trace attributes include `deployment.environment=local`.

#### 8. Build Multi-Platform Images (ARM64 & AMD64)
If you are developing on a Mac (ARM64) but need to deploy to Windows/Linux (AMD64), use:

```bash
docker buildx bake --push
```

*Note: This command uses the settings in `docker-compose.yml` to build both backend and frontend for both architectures and push them to Docker Hub.*

---

### B. Database for Local Development (Alternative)

If you prefer to run the backend or frontend locally (not in Docker) while still using a containerized database:

1) Quick builds/tests (no Docker/MySQL required)
- The Maven build is configured so unit tests do NOT require a running database. You can run:
  - `mvn -f job-portal-backend clean verify`
- The application itself (when you actually run it) still expects MySQL as configured in `application.yml`.

2) Run only MySQL using Docker Compose
- Start the MySQL database:
  ```bash
  docker compose up -d db
  ```
- Once the database is running, you can run the applications:
  - **Backend (IDE)**: Run `JobportalApplication` main class.
  - **Backend (Maven)**: `mvn spring-boot:run` inside `job-portal-backend`.
  - **Frontend (npm)**: `npm install` and `npm run dev` inside `job-portal-frontend`.

---

### C. Environment Configuration:

The project is configured with safe defaults for local development. However, you can override them by creating a file named `.env` in the root directory:

```env
# Common MySQL credentials
MYSQL_ROOT_PASSWORD=root
MYSQL_DATABASE=jobportal
MYSQL_USER=jobuser
MYSQL_PASSWORD=jobpass

# Google OAuth2 Credentials (Optional, required for Google Login)
GOOGLE_CLIENT_ID=your-google-client-id
GOOGLE_CLIENT_SECRET=your-google-client-secret

# JWT Secret Key (Optional, uses default if not provided)
JWT_SECRET_KEY=MY_SECRET_KEY_123456789012345678901234567890

# Allowed Origins for CORS (Optional, defaults to http://localhost:5173)
ALLOWED_ORIGINS=http://localhost:5173,http://your-ec2-ip:5173
```

---

### D. Steps to smoke test the Google Cloud OAuth feature:

1. **Go to Google Cloud** and get the secrets for your Web Application (not Desktop, not Mobile application):
   - **Client ID**: `xxxx`
   - **Client Secret**: `yyyy`

   Enter these two values in the `.env` file at the root of the project:
   ```env
   GOOGLE_CLIENT_ID=xxxx
   GOOGLE_CLIENT_SECRET=yyyy
   ```

   The `application.yml` file is configured to use these environment variables.

2. **Go to**: [http://localhost:8080/oauth2/authorization/google](http://localhost:8080/oauth2/authorization/google) and enter your Google credentials.

3. **Go to**: [http://localhost:8080/api/oauth/user](http://localhost:8080/api/oauth/user) and get a Google OpenID Connect ID token payload like this:
   ```json
   {
     "at_hash": "aaa",
     "sub": "bbb",
     "email_verified": true,
     "iss": "https://accounts.google.com",
     "given_name": "ccc",
     "nonce": "ddd",
     "picture": "https://lh3.googleusercontent.com/a/eee",
     "aud": [
       "fff.apps.googleusercontent.com"
     ],
     "azp": "ggg.apps.googleusercontent.com",
     "name": "hhh",
     "exp": "2026-02-01T17:58:45Z",
     "family_name": "iii",
     "iat": "2026-02-01T16:58:45Z",
     "email": "jjjj@test.com"
   }
   ```

4. **Go to**: [http://localhost:8080/api/oauth/token](http://localhost:8080/api/oauth/token) and get a JWT token like this:
   `eyJhbGc29udGVudC5jb20iLCJz...dWIiOiIxMTM0NTc2NjA1NjY`

   You can paste this token on [jwt.io](https://www.jwt.io/) to decode it. You will get something like this:
   ```json
   {
     "alg": "RS256",
     "kid": "c27...122",
     "typ": "JWT"
   }
   {
     "iss": "https://accounts.google.com",
     "azp": "1...a.apps.googleusercontent.com",
     "aud": "1.....apps.googleusercontent.com",
     "sub": "11111",
     "email": "xxx@test.com",
     "email_verified": true,
     "at_hash": "zzzz",
     "nonce": "....",
     "name": "ABC",
     "picture": "https://googleusercontent.com/....",
     "given_name": "X",
     "family_name": "Y",
     "iat": 1770227337,
     "exp": 1770230937
   }
   {
     "e": "AQAB",
     "kty": "RSA",
     "n": "ubOB3C56t2P...mrXJrc2Ws2sizhSqjrPzL"
   }
   ```

5. **Test other endpoints**:
   - [http://localhost:8080/api/oauth/exchange-token](http://localhost:8080/api/oauth/exchange-token)
   - [http://localhost:8080/api/oauth/user-details](http://localhost:8080/api/oauth/user-details)

---

### D. Steps for smoke test the backend API:

1. **Load the Insomnia collection** (inside the folder `doc/insomnia`).
2. **Execute the "add user" POST request** to add a new user:
   ```json
   {
     "name": "user",
     "email": "user@test.com",
     "password": "user123",
     "authProvider": "LOCAL",
     "role": "APPLICANT"
   }
   ```
3. **Execute the "add job" POST request** to add a new job:
   Use Basic Auth with `admin` / `admin123` (ROLE_ADMIN). The default `user` / `user123` (ROLE_APPLICANT) will receive 403 Forbidden for this endpoint by design.
   ```json
   {
     "title": "Java Developer",
     "description": "Develop java applications",
     "company": "XYZ"
   }
   ```
---

### E. Integration Test

1. **Start the front end**:
   Go to the folder `job-portal-frontend` and type:
   ```bash
   npm install
   npm run dev
   ```
2. **Go to the URL of the Web Application**: [http://localhost:5173](http://localhost:5173)
3. **Login with credentials**: `user`, `user123` and run tests.
4. **Try to apply for a job**. Check the database running the queries on `doc/queries.sql`.

---

### G. Helper Scripts for Deployment

To make it easier to manage the application on your EC2 instance, you can create these two helper scripts in the project root:

#### 1. `start.sh`
This script ensures you always have the latest images and starts the application in the background.
```bash
#!/bin/bash
docker compose pull
docker compose up -d
```

#### 2. `stop.sh`
This script stops and removes the containers.
```bash
#!/bin/bash
docker compose down
```

#### 3. Make them executable
After creating the files, run:
```bash
chmod +x start.sh stop.sh
```

### H. Do you know Qodana?

Static code analysis by Qodana helps development teams follow agreed quality standards, and deliver readable, 
maintainable, and secure code. Powered by JetBrains.

[https://www.jetbrains.com/qodana/](https://www.jetbrains.com/qodana/)

Configure the tool using this file:

`.github/workflows/qodana_code_quality.yml`

and then 

```bash
job-portal-backend % QODANA_TOKEN="eyJhbGciOiJI.....get.the.token.from.JetBrains.....aNgCIgbs_d5GVlX1srJNqDrzDtYA" \
qodana scan
```

At the end of the process you will get something like:

```text
? Do you want to open the latest report [Y/n]Yes
✓ Report is successfully uploaded to https://qodana.cloud/projects/40Edn/reports/xyz
```
