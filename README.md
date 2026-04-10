# Barts Clinical Trial Platform

A full-stack web application for managing clinical research studies and patient
recruitment, built for the Barts Cancer Institute technical assessment.

## Prerequisites

| Tool    | Version | Install                           |
| ------- | ------- | --------------------------------- |
| Java    | 21      | `sudo apt install openjdk-21-jdk` |
| Maven   | 3.9+    | `sudo apt install maven`          |
| Node.js | 20+     | `sudo apt install nodejs`         |
| Git     | Any     | `sudo apt install git`            |

## Clone the Repository

```bash

git clone https://github.com/your-username/clinical-trial-platform.git
cd clinical-trial-platform
```

## Tech stack

| Layer    | Technology                              |
| -------- | --------------------------------------- |
| Backend  | Java 21, Spring Boot 3, H2, JPA         |
| Frontend | React 18, TypeScript, React Query, Vite |
| Tests    | JUnit 5, Mockito, MockMvc, Vitest, RTL  |
| Deploy   | Render (backend) + Netlify (frontend)   |
| CI       | GitHub Actions                          |

## Running locally

### Prerequisites

- Java 21
- Maven 3.9+
- Node 20+

### Backend

```bash

cd backend
mvn package -DskipTests -q
java -jar target/clinical-trial-platform-1.0.0.jar
```

API runs at http://localhost:8080/api
Seed data loads automatically on startup.

### Frontend

```bash


cd frontend
cp .env.example .env.local
npm install
npm run dev
```

App runs at http://localhost:5173

## Running tests

```bash


# Backend unit + integration tests
cd backend && mvn test

# Frontend component tests
cd frontend && npm run test
```

## Deployment

| Layer    | Platform | Trigger        |
| -------- | -------- | -------------- |
| Backend  | Render   | Push to `main` |
| Frontend | Netlify  | Push to `main` |

### Render (backend)

1. New Web Service → connect GitHub repo
2. Root directory: `backend/`
3. Runtime: **Docker** (reads `backend/Dockerfile` automatically)
4. Environment variables:
   - `FRONTEND_URL` = `https://your-app.netlify.app`
   - `SPRING_PROFILES_ACTIVE` = `prod`

### Netlify (frontend)

1. New site → connect GitHub repo
2. Netlify auto-detects `frontend/netlify.toml` — no manual config needed
3. Environment variables:
   - `VITE_API_BASE_URL` = `https://your-app.onrender.com/api`

## Design decisions

**Pessimistic over optimistic locking** — For the last-slot race condition,
pessimistic locking (SELECT FOR UPDATE) is used instead of optimistic. This
eliminates retry logic complexity: the losing thread fails fast with a clean
409 rather than requiring application-level retry loops.

**React Query over Redux** — Studies and patients are pure server state. React
Query handles caching, background refetch, loading/error states, and cache
invalidation with minimal code. Redux would add boilerplate with no benefit.

**Dedicated RecruitmentService and controller** — Recruitment is a command (an
action), not a CRUD operation. Keeping it separate from the patient/study
controllers makes the transaction boundary explicit, testable in isolation, and
easy to extend with new guards or events.

**H2 in-memory database** — Zero external infrastructure for the demo. The
`data.sql` seed file populates realistic data on every startup so the assessor
sees a working UI immediately without any manual setup.

## What I would improve with more time

- Replace H2 with PostgreSQL on Render's managed database tier
- Add WebSocket support for live enrollment count updates
- Implement role-based access (admin vs researcher) with Spring Security
- Add soft deletes on patients and studies
- Build an advanced eligibility rules engine (JSON-based rules, not string parsing)
- Add OpenAPI/Swagger documentation via springdoc-openapi
