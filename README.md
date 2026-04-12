# Barts Clinical Trial Platform

A full-stack web application for managing clinical research studies and patient recruitment, built for the Barts Cancer Institute

## Live Application

| Layer    | URL                                                  |
| -------- | ---------------------------------------------------- |
| Frontend | https://clinical-trial-platform-frontend.netlify.app |
| Backend  | render.com                                           |

## Browser Recommendation
**Recommended browser: Mozilla Firefox.

** Chrome, Safari and Edge block cross-origin session cookies by default. See [Known Issues](#known-issues) below.

API is deployed on Render and consumed by the frontend automatically.
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

## Known Issues

Chrome, Safari and Edge return 401 on login when using the live deployment.

This is caused by cross-origin session cookie blocking (`SameSite` policy).

The frontend is on `netlify.app` and the backend is on `onrender.com`, different domains.

Browsers block the `JSESSIONID` cookie unless it is explicitly set with `SameSite=None; Secure`.

Firefox is more lenient and works correctly.

### What I Would Improve With More Time

Replace H2 with PostgreSQL on Render managed database

OpenAPI documentation via springdoc-openapi

Audit trail: who recruited which patient and when

Pact consumer-driven contract tests between frontend and backend

Multi-module Maven structure with enforced architectural boundaries

Fix cross-browser session cookie issue by setting `SameSite=None; Secure; HttpOnly` on the session cookie

Migrating auth to JWT with `Authorization` header to avoid cookie cross-origin restrictions entirely


