# Solution Design

**Position:** Research Software Engineer

**Organisation:** Barts Cancer Institute, Queen Mary University of London

---

## Table of Contents

1. [Problem Understanding](#1-problem-understanding)
2. [Architecture](#2-architecture)
3. [Domain Model](#3-domain-model)
4. [Backend Design](#4-backend-design)
5. [Frontend Design](#5-frontend-design)
6. [Concurrency Handling](#6-concurrency-handling)
7. [Testing Strategy](#7-testing-strategy)

## 1. Problem Understanding

Three constraints drove every design decision:

**Study lifecycle integrity.** A study moves through a strict one-way sequence: DRAFT, OPEN, CLOSED, ARCHIVED. Enforced at the API layer, not just the UI.

**Recruitment correctness.** A patient can only be recruited into an OPEN study, must not already be enrolled elsewhere, must meet eligibility criteria, and the study must not be at capacity. All guards fire in order and fail with a typed error.

**Concurrency safety.** Two clinicians simultaneously recruiting the last available slot is a real patient safety risk. Prevented at the database level, not in application code.

---

## 2. Architecture

Three-tier web application:

- React SPA served by Netlify
- Spring Boot REST API on Render via Docker
- H2 in-memory database (assessment scope)

The frontend communicates exclusively through the REST API. In local development, Vite proxies `/api` requests to port 8080. In production, CORS is configured to allow only the Netlify origin.

---

## 3. Domain Model

**Study** — id, title, status, maxEnrollment, currentEnrollment, eligibilityCriteria, lastRecruitedAt, version

**Patient** — id, name, age, condition, enrolledStudyId, deleted

A patient holds the foreign key to its enrolled study. Enrollment is a property of the patient, not a join table, because it has no independent lifecycle.

StudyStatus transitions are enforced in the service layer. Soft deletes are implemented on both entities — deleted records are excluded from all queries but retained in the database for audit purposes.

---

## 4. Backend Design

### Layers

**Controller** — HTTP only. Parses requests, validates with Bean Validation, delegates to service, returns correct status codes. No business logic.

**Service** — All business logic. Enforces lifecycle transitions, recruitment guards, eligibility checking. Throws typed exceptions mapped to HTTP responses by the global exception handler.

**Repository** — Spring Data JPA. Custom method `findByIdForUpdate` issues `SELECT FOR UPDATE` for pessimistic locking during recruitment.

**DTOs** — Used at every API boundary. Domain entities are never serialised directly to the client.

### Recruitment Guard Chain

Guards fire in order, cheapest first:

1. Load patient and study. Lock study row with `SELECT FOR UPDATE`. Return 404 if either not found.
2. Check study is OPEN. Return 409 if not.
3. Check patient is not already enrolled. Return 409 if enrolled.
4. Check enrollment capacity. Return 409 if full.
5. Check eligibility criteria. Return 422 if patient does not qualify.
6. Enroll patient, increment count, emit `PatientRecruitedEvent`.

### Security

Spring Security with session-based authentication and two in-memory users:

| Username     | Role              |
| ------------ | ----------------- |
| `admin`      | `ROLE_ADMIN`      |
| `researcher` | `ROLE_RESEARCHER` |

All mutating endpoints (POST, PUT, PATCH, DELETE) on studies and patients require `ROLE_ADMIN`. Researchers can read all data and post to `/api/recruitment`. `AccessDeniedException` is re-thrown by the global exception handler so Spring Security returns 403 correctly.

### Eligibility Engine

Rules are parsed from the `eligibilityCriteria` string by a dedicated `EligibilityEngine`. Supported rules:

- Age range: `age>18`, `age<65`
- Condition matching: `condition=NSCLC`
- OR logic: multiple conditions separated by comma

Each rule is a typed `EligibilityRule` implementation. Failures return a specific reason, not a generic message.

### Error Handling

`GlobalExceptionHandler` maps every typed exception to a consistent JSON envelope:

```json
{
  "timestamp": "2026-04-12T09:00:00Z",
  "status": 409,
  "error": "Conflict",
  "message": "Study 1 is not OPEN for recruitment"
}
```

## 5. Frontend Design

Structure

    AuthContext — global auth state, login/logout, session persistence
    ProtectedRoute — redirects unauthenticated users to /login
    LoginPage — form with validation and error handling
    StudyTable / PatientTable — hide mutating controls for researchers
    RecruitModal — recruit a patient into an open study

State Management

React Query handles all server state. It provides caching, background refetch, loading and error states, and cache invalidation after mutations. Polling is used for live enrollment count updates without requiring a WebSocket connection. Redux would add boilerplate with no benefit for a data-driven application where all meaningful state lives on the server.
UX

    Loading spinners on every in-flight request
    Error banners display the API message directly
    Form validation runs client-side before submission
    Role-aware UI: admin sees all controls, researcher sees read-only view with recruit action visible

## 6. Concurrency Handling

Problem. Two users read a study with one slot remaining. Both see capacity available. Both recruit. Study is over-enrolled.

Solution. Pessimistic locking with SELECT FOR UPDATE. The study row is locked when the recruitment transaction starts. The second transaction blocks until the first commits, then reads the updated count, finds the study full, and returns 409.

Why pessimistic over optimistic. Optimistic locking requires catching OptimisticLockException and implementing retry logic. In a clinical context, a silent retry is harder to reason about and debug. Pessimistic locking fails fast: one transaction wins, one loses, no ambiguity.

## 7. Testing Strategy

Backend — 67 tests

Service tests (unit) — Mockito mocks the repository layer. Every recruitment guard, lifecycle transition, and eligibility rule is tested in isolation with boundary conditions.

Controller tests (integration) — MockMvc with full Spring context. Verifies HTTP status codes, response body shape, error envelope format, and role enforcement.

Key edge cases covered:

    Over-enrollment when study is at capacity
    Invalid state transitions
    Duplicate enrollment
    Ineligible patient recruitment
    Researcher blocked from mutating endpoints (403)

Frontend — 21 tests

Vitest and React Testing Library. API calls mocked with Vitest module mocking. Tests cover rendering, form validation, role-based control visibility, and mutation call signatures.
