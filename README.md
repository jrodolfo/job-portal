# Job Portal - Backend and Frontend

A good portion of this code came from the Pluralsight online course:

**Full-stack Java Development with Spring Boot 3 and React**
by Imtiyaz Hirani

[Course Link](https://app.pluralsight.com/ilx/video-courses/full-stack-java-development-spring-boot-3-react)

I made several changes, fixed bugs, added new files, documentation etc. to help me to
run a clean code and deploy it at my localhost and at AWS.

**Rod Oliveira** | Software Developer | [jrodolfo.net](https://jrodolfo.net) | Halifax, Canada 🍁

---

### A. Environment Configuration:

To run this project locally using Docker, you need to create a file named `.env` in the root directory and enter 
the following credentials:

```env
# Common MySQL credentials
MYSQL_ROOT_PASSWORD=rootpassword
MYSQL_DATABASE=jobportal
MYSQL_USER=jobuser
MYSQL_PASSWORD=jobpass

# Google OAuth2 Credentials (Optional, required for Google Login)
GOOGLE_CLIENT_ID=your-google-client-id
GOOGLE_CLIENT_SECRET=your-google-client-secret

# JWT Secret Key (Optional, uses default if not provided)
JWT_SECRET_KEY=your-secure-jwt-secret-key-at-least-32-chars
```

---

### B. Steps to smoke test the Google Cloud OAuth feature:

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

### C. Steps for smoke test the backend API:

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
   ```json
   {
     "title": "Java Developer",
     "description": "Develop java applications",
     "company": "XYZ"
   }
   ```

---

### D. Database for Local Development

You have two options when working on the backend locally:

1) Quick builds/tests (no Docker/MySQL required)
- The Maven build is configured so unit tests do NOT require a running database. You can run:
  - `mvn -f job-portal-backend clean verify`
- The application itself (when you actually run it) still expects MySQL as configured in `application.yml`.

2) Run MySQL in Docker (recommended for running the app locally)
- Start a MySQL container for the Job Portal backend with a non-root user:

```
# Remove any previous container with the same name (safe to ignore errors)
docker rm -f mysql-jobportal

# Start MySQL on localhost port 3307
# Credentials match the defaults in application.yml
#   user: jobuser
#   password: jobpass
#   database: jobportal

docker run -d \
  --name mysql-jobportal \
  -e MYSQL_ROOT_PASSWORD=root \
  -e MYSQL_DATABASE=jobportal \
  -e MYSQL_USER=jobuser \
  -e MYSQL_PASSWORD=jobpass \
  -p 3307:3306 \
  mysql:latest
```

- Then run the backend (outside Docker) from `job-portal-backend`:
  - `mvn spring-boot:run`
- Or use docker-compose to bring up db + backend + frontend together:
  - `docker compose up --build`

This mirrors production-like setups (non-root DB user, isolated containerized DB), while keeping builds smooth.

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

### E. Static Code Analysis with Qodana

Do you know Qodana?

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
