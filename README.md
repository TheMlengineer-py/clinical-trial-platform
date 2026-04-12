# Barts Clinical Trial Platform

A full-stack web application for managing clinical research studies and patient recruitment, built for the Barts Cancer Institute

## Live Application

| Layer    | URL                                                  |
| -------- | ---------------------------------------------------- |
| Frontend | https://clinical-trial-platform-frontend.netlify.app |
| Backend  | render.com                                           |

The backend API is deployed on Render and consumed by the frontend automatically.
No direct API access is required to evaluate the application.

## Tech Stack

| Layer    | Technology                                               |
| -------- | -------------------------------------------------------- |
| Backend  | Java 21, Spring Boot 3, Spring Security, H2, JPA         |
| Frontend | React 18, TypeScript, React Query, Vite                  |
| Tests    | JUnit 5, Mockito, MockMvc, Vitest, React Testing Library |
| Deploy   | Render (backend), Netlify (frontend)                     |
| CI       | GitHub Actions                                           |

## Prerequisites

| Tool    | Version |
| ------- | ------- |
| Java    | 21      |
| Maven   | 3.9+    |
| Node.js | 20+     |
| Git     | Any     |

## Running Locally

### 1. Clone Repository

```bash

git clone https://github.com/TheMlengineer-py/clinical-trial-platform.git
cd clinical-trial-platform
```

### 2. Backend (terminal 1)

```bash

cd backend
mvn package -DskipTests -q
java -jar target/clinical-trial-platform-1.0.0.jar
```

API runs at http://localhost:8080/api. Seed data loads automatically on startup.

### Frontend (terminal 2)

```bash

cd frontend
cp .env.example .env.local
npm install
npm run dev
```

App runs at http://localhost:5173.

### Login Credentials

```
Role : Admin
Username = admin
Password = password

Role: Researcher
Username = researcher
Password = password
```

### Role Permissions

Admin — full access

    Create, edit, delete studies and patients
    Manage study lifecycle (DRAFT, OPEN, CLOSED, ARCHIVED)
    Recruit patients into open studies

Researcher — limited access

    View studies and patients (read only)
    Recruit patients into open studies
    No create, edit or delete access

### Running Tests

### Backend

```bash

cd backend
mvn test

# Summary view
mvn test 2>&1 | grep -E "Tests run|BUILD|FAILURE|ERROR" | tail -15
```

Expected: 67 tests, 0 failures

### Frontend

```bash

cd frontend
npm test
```

Expected: 21 tests, 0 failures

### Features Implemented

### Core Requirements

Study CRUD with pagination, filtering by status, sorting by most recent recruitment
Patient CRUD with pagination and filtering by condition
Study lifecycle enforcement: DRAFT, OPEN, CLOSED, ARCHIVED (strict one-way transitions)
Patient recruitment with full guard chain: study must be OPEN, patient not already enrolled, eligibility criteria met, capacity not exceeded
Concurrency handling: pessimistic locking prevents over-enrollment in last-slot race conditions
Domain event emitted on every successful recruitment
Loading states, error handling and form validation throughout the UI

### Bonus Features (all implemented)

Feature Implementation
Role-based access - Spring Security with ADMIN and RESEARCHER roles, enforced at API and UI layer
Advanced eligibility engine - Age range and OR condition matching, typed failure reasons per rule
Docker setuP - Multi-stage Dockerfile, used by Render for production deployment
CI pipeline - GitHub Actions, path-scoped backend and frontend workflows on every push
WebSocket / polling - React Query polling for live enrollment count updates
Soft deletes - Studies and patients are archived rather than permanently deleted

### Design Decisions

Pessimistic locking over optimistic — The last-slot race condition is handled with SELECT FOR UPDATE. The losing thread fails immediately with a 409 rather than requiring application-level retry logic. In a clinical context, correctness outweighs throughput.

React Query over Redux — Studies and patients are server state. React Query handles caching, background refetch, loading and error states, and cache invalidation after mutations. Redux adds boilerplate with no benefit for this pattern.

Dedicated recruitment endpoint — Recruitment is a command with domain rules and side effects, not a CRUD operation. A separate /api/recruitment endpoint keeps the intent explicit and the service independently testable.

Session-based auth over JWT — Spring Security session cookies work naturally with the browser. No token storage or refresh logic required on the frontend.

String-based eligibility criteria — Format age>18,condition=NSCLC is parsed by a dedicated rules engine with typed failure reasons. OR logic is supported for multiple conditions.

### Trade-offs and Assumptions

H2 in-memory database resets on every restart. Seed data provides a consistent starting state. Production would use PostgreSQL.
Single-module Maven project for simplicity. Production would separate domain, application, and infrastructure into modules with explicit dependency rules.
Soft deletes implemented at the service layer. A production system would add database-level filtering to exclude deleted records from all queries by default.

### What I Would Improve With More Time

Replace H2 with PostgreSQL on Render managed database
OpenAPI documentation via springdoc-openapi
Audit trail: who recruited which patient and when
Pact consumer-driven contract tests between frontend and backend
Multi-module Maven structure with enforced architectural boundaries

[//]: # "# Barts Clinical Trial Platform"
[//]: #
[//]: # "A full-stack web application for managing clinical research studies and patient"
[//]: # "recruitment, built for the Barts Cancer Institute technical assessment."
[//]: #
[//]: # "## Prerequisites"
[//]: #
[//]: # "| Tool    | Version | Install                           |"
[//]: # "| ------- | ------- | --------------------------------- |"
[//]: # "| Java    | 21      | `sudo apt install openjdk-21-jdk` |"
[//]: # "| Maven   | 3.9+    | `sudo apt install maven`          |"
[//]: # "| Node.js | 20+     | `sudo apt install nodejs`         |"
[//]: # "| Git     | Any     | `sudo apt install git`            |"
[//]: #
[//]: # "## Clone the Repository"
[//]: #
[//]: # "```bash"
[//]: #
[//]: # "git clone https://github.com/your-username/clinical-trial-platform.git"
[//]: # "cd clinical-trial-platform"
[//]: # "```"
[//]: #
[//]: # "## Tech stack"
[//]: #
[//]: # "| Layer    | Technology                              |"
[//]: # "| -------- | --------------------------------------- |"
[//]: # "| Backend  | Java 21, Spring Boot 3, H2, JPA         |"
[//]: # "| Frontend | React 18, TypeScript, React Query, Vite |"
[//]: # "| Tests    | JUnit 5, Mockito, MockMvc, Vitest, RTL  |"
[//]: # "| Deploy   | Render (backend) + Netlify (frontend)   |"
[//]: # "| CI       | GitHub Actions                          |"
[//]: #
[//]: # "## Running locally"
[//]: #
[//]: # "### Prerequisites"
[//]: #
[//]: # "- Java 21"
[//]: # "- Maven 3.9+"
[//]: # "- Node 20+"
[//]: #
[//]: # "### Backend"
[//]: #
[//]: # "```bash"
[//]: #
[//]: # "cd backend"
[//]: # "mvn package -DskipTests -q"
[//]: # "java -jar target/clinical-trial-platform-1.0.0.jar"
[//]: # "```"
[//]: #
[//]: # "API runs at http://localhost:8080/api"
[//]: # "Seed data loads automatically on startup."
[//]: #
[//]: # "### Frontend"
[//]: #
[//]: # "```bash"
[//]: #
[//]: #
[//]: # "cd frontend"
[//]: # "cp .env.example .env.local"
[//]: # "npm install"
[//]: # "npm run dev"
[//]: # "```"
[//]: #
[//]: # "App runs at http://localhost:5173"
[//]: #
[//]: # "## Running tests"
[//]: #
[//]: # "```bash"
[//]: #
[//]: #
[//]: # "# Backend unit + integration tests"
[//]: # "cd backend && mvn test"
[//]: #
[//]: # "# Frontend component tests"
[//]: # "cd frontend && npm run test"
[//]: # "```"
[//]: #
[//]: # "## Deployment"
[//]: #
[//]: # "| Layer    | Platform | Trigger        |"
[//]: # "| -------- | -------- | -------------- |"
[//]: # "| Backend  | Render   | Push to `main` |"
[//]: # "| Frontend | Netlify  | Push to `main` |"
[//]: #
[//]: # "### Render (backend)"
[//]: #
[//]: # "1. New Web Service → connect GitHub repo"
[//]: # "2. Root directory: `backend/`"
[//]: # "3. Runtime: **Docker** (reads `backend/Dockerfile` automatically)"
[//]: # "4. Environment variables:"
[//]: # "   - `FRONTEND_URL` = `https://your-app.netlify.app`"
[//]: # "   - `SPRING_PROFILES_ACTIVE` = `prod`"
[//]: #
[//]: # "### Netlify (frontend)"
[//]: #
[//]: # "1. New site → connect GitHub repo"
[//]: # "2. Netlify auto-detects `frontend/netlify.toml` — no manual config needed"
[//]: # "3. Environment variables:"
[//]: # "   - `VITE_API_BASE_URL` = `https://your-app.onrender.com/api`"
[//]: #
[//]: # "## Design decisions"
[//]: #
[//]: # "**Pessimistic over optimistic locking** — For the last-slot race condition,"
[//]: # "pessimistic locking (SELECT FOR UPDATE) is used instead of optimistic. This"
[//]: # "eliminates retry logic complexity: the losing thread fails fast with a clean"
[//]: # "409 rather than requiring application-level retry loops."
[//]: #
[//]: # "**React Query over Redux** — Studies and patients are pure server state. React"
[//]: # "Query handles caching, background refetch, loading/error states, and cache"
[//]: # "invalidation with minimal code. Redux would add boilerplate with no benefit."
[//]: #
[//]: # "**Dedicated RecruitmentService and controller** — Recruitment is a command (an"
[//]: # "action), not a CRUD operation. Keeping it separate from the patient/study"
[//]: # "controllers makes the transaction boundary explicit, testable in isolation, and"
[//]: # "easy to extend with new guards or events."
[//]: #
[//]: # "**H2 in-memory database** — Zero external infrastructure for the demo. The"
[//]: # "`data.sql` seed file populates realistic data on every startup so the assessor"
[//]: # "sees a working UI immediately without any manual setup."
[//]: #
[//]: # "## What I would improve with more time"
[//]: #
[//]: # "- Replace H2 with PostgreSQL on Render's managed database tier"
[//]: # "- Add WebSocket support for live enrollment count updates"
[//]: # "- Implement role-based access (admin vs researcher) with Spring Security"
[//]: # "- Add soft deletes on patients and studies"
[//]: # "- Build an advanced eligibility rules engine (JSON-based rules, not string parsing)"
[//]: # "- Add OpenAPI/Swagger documentation via springdoc-openapi"
