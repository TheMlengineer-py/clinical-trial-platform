# Solution Design Document

## Barts Clinical Trial Platform

> **Assessment:** Research Software Engineer Technical Assessment
> **Organisation:** Barts Cancer Institute, Queen Mary University of London

---

## Table of Contents

1. [Problem Understanding](#1-problem-understanding)
2. [Architecture Overview](#2-architecture-overview)
3. [Domain Modelling](#3-domain-modelling)
4. [Backend Design](#4-backend-design)
5. [Frontend Design](#5-frontend-design)
6. [Concurrency Handling](#6-concurrency-handling)
7. [Testing Strategy](#7-testing-strategy)
8. [Design Decisions](#8-design-decisions)
9. [Trade-offs and Assumptions](#9-trade-offs-and-assumptions)
10. [What I Would Improve With More Time](#10-what-i-would-improve-with-more-time)
11. [Bonus Features Implemented](#11-bonus-features-implemented)

---

## 1. Problem Understanding

The core challenge is not simply building a CRUD application. It is building a
system that enforces real-world clinical trial constraints with correctness and
safety guarantees.

Three constraints drove every design decision:

**Constraint 1 — Study lifecycle integrity.** A study moves through a strict
one-way sequence: DRAFT to OPEN to CLOSED to ARCHIVED. No skipping, no
reversals. This is not just a UI concern — it must be enforced at the API
layer so no client, script, or concurrent request can put a study into an
invalid state.

**Constraint 2 — Recruitment correctness.** A patient can only be recruited
into an OPEN study, must not already be enrolled elsewhere, must meet the
eligibility criteria, and the study must not be at capacity. All five of these
guards must fire in the correct order and fail with a meaningful error if
violated.

**Constraint 3 — Concurrency safety.** The last-slot scenario — two clinicians
simultaneously recruiting the last available patient slot — is a real patient
safety risk. If both read the current enrollment count before either writes,
both will believe capacity is available and both will succeed, resulting in
over-enrollment. This must be prevented at the database level, not in
application code.

---

## 2. Architecture Overview

The system is a standard three-tier web application: a React single-page
application served by Netlify, a Spring Boot REST API deployed on Render via
Docker, and an H2 in-memory database for the assessment scope.

The tiers are loosely coupled by design. The frontend communicates exclusively
through the REST API — it has no knowledge of the database schema, entity
structure, or business rules. The backend exposes a stable JSON contract and
enforces all domain rules internally. This means the database can be swapped
from H2 to PostgreSQL, or the frontend can be rewritten in Vue, without
changing either counterpart.

In local development, the Vite dev server proxies all requests on the `/api`
path to the Spring Boot server on port 8080. This eliminates CORS complexity
during development and means the frontend code never hard-codes a port number.
In production, CORS is configured explicitly to allow only the known Netlify
origin.

The separation between local development and production is managed entirely
through environment variables and Spring profiles. No code changes are required
to move from development to production — only configuration.

---

## 3. Domain Modelling

The domain has two core entities: Study and Patient. Their relationship is
intentionally simple and asymmetric — a study can have many patients, but a
patient can only be enrolled in one study at a time. This is modelled as a
nullable foreign key on the Patient entity rather than a join table, because
enrollment is a property of the patient, not a relationship requiring its own
lifecycle.

StudyStatus is an enum with four values: DRAFT, OPEN, CLOSED, ARCHIVED. The
transition rules are enforced in the service layer as a guard, not in the
database. This keeps the transition logic readable, testable in isolation, and
easy to extend without a schema migration.

The Study entity carries a version field used for pessimistic locking. The
lastRecruitedAt timestamp is updated on every successful recruitment and drives
the default sort order — most recently recruited studies appear first. This
directly addresses the assessment requirement for sorting by most recent
recruitment.

The eligibilityCriteria field is a simple string in the format
`age>18,condition=NSCLC`. This was a deliberate trade-off: a full rules engine
would require significantly more implementation time, and the string-based
approach is sufficient to demonstrate the eligibility checking concept while
remaining readable and testable.

---

## 4. Backend Design

### Layering and Separation of Concerns

The backend is structured in strict layers, each with a single responsibility.

The controller layer handles only HTTP concerns: parsing the incoming request,
validating the request body using Bean Validation annotations, delegating to
the service layer, and returning the correct HTTP status code and response
body. Controllers have no business logic.

The service layer contains all business logic. It enforces lifecycle
transitions, recruitment guards, and eligibility checking. Services are
completely unaware of HTTP — they throw typed exceptions, and the global
exception handler maps those exceptions to HTTP responses. This means the
service layer can be tested with pure unit tests using Mockito, with no Spring
context required.

The repository layer is a thin Spring Data JPA interface. The only custom
method is `findByIdForUpdate`, which issues a `SELECT FOR UPDATE` query for
pessimistic locking during recruitment.

DTOs are used consistently at the API boundary. Domain entities are never
returned directly to the client. This prevents accidental exposure of internal
fields and decouples the API contract from the database schema.

### Recruitment Guard Chain

The recruitment flow is the most critical path in the system. The guards fire
in a specific order designed to fail fast on the cheapest checks first:

First, the patient and study are loaded. If either does not exist, a 404 is
returned immediately. The study is loaded with a `SELECT FOR UPDATE` lock at
this point, which is essential — the lock must be held for the entire
transaction, not acquired after the capacity check.

Second, the study status is checked. If the study is not OPEN, a 409 Conflict
is returned. This check happens before the patient check because it is
independent of the patient and can fail immediately.

Third, the patient's current enrollment is checked. If they are already
enrolled in any study, a 409 is returned.

Fourth, the enrollment capacity is checked. If the study is full, a 409 is
returned.

Fifth, the eligibility criteria are checked. If the patient does not meet the
criteria, a 422 Unprocessable Entity is returned — this is semantically
distinct from a conflict, it means the request is well-formed but cannot be
fulfilled.

Finally, if all guards pass, the patient is enrolled, the study count is
incremented, and a `PatientRecruitedEvent` is emitted.

### Domain Events

`PatientRecruitedEvent` is emitted via `DomainEventPublisher` on every
successful recruitment. The current implementation logs the event. The
architecture is designed so this can be replaced with a Kafka or RabbitMQ
publisher without any changes to `RecruitmentService`. The service depends on
the `DomainEventPublisher` abstraction, not the logging implementation.

### Global Exception Handler

`GlobalExceptionHandler` is a `@RestControllerAdvice` that intercepts every
exception thrown anywhere in the application and converts it to a consistent
JSON error envelope with a timestamp, status code, HTTP reason phrase, and
message. This means the React frontend can always parse error responses
identically, regardless of which guard or layer threw the exception.

### Pagination, Filtering, and Sorting

Studies support filtering by status and sorting by lastRecruitedAt descending
by default. Patients support filtering by condition. Both resources are
paginated with configurable page size. These are implemented using Spring Data
JPA's Pageable and Specification support, keeping the repository layer clean.

---

## 5. Frontend Design

### Component Structure

The frontend is divided into two pages: Studies and Patients. Each page
contains a table component that displays the data, and modal components for
creating, editing, and recruiting.

The StudyTable renders the lifecycle action buttons conditionally based on the
current study status. An OPEN study shows a Close button. A DRAFT study shows
an Open for Recruitment button. A CLOSED study shows an Archive button. This
keeps the UI consistent with the backend lifecycle rules without duplicating
the transition logic in the frontend — the backend will reject invalid
transitions regardless of what the frontend sends.

The RecruitModal allows a clinician to select a study and recruit a patient.
The available studies are filtered to OPEN status only on the backend, so the
dropdown never shows ineligible studies.

### State Management

React Query was chosen over Redux or Context for state management. Studies and
patients are entirely server state — they live in the database, not in the
client. React Query is purpose-built for this pattern. It handles fetching,
caching, background refetching, loading states, error states, and cache
invalidation after mutations with minimal boilerplate.

Redux would require manually managing loading flags, error states, and cache
invalidation — all problems React Query solves by default. Context would not
provide caching or background refetch. For a data-driven application like this,
React Query is the right tool.

### UX Decisions

Loading spinners are shown while any query is in flight. Error banners display
the API error message directly — because the backend returns human-readable
messages like "Study 3 is not OPEN for recruitment", the frontend does not need
to map error codes to messages. Form validation runs client-side before
submission to catch empty required fields immediately, reducing unnecessary API
calls.

---

## 6. Concurrency Handling

The last-slot race condition is the most technically interesting constraint in
the assessment. Two users simultaneously viewing a study with one slot
remaining will both see capacity as available. Without a concurrency control
mechanism, both recruits will succeed and the study will be over-enrolled.

The solution is pessimistic locking using `SELECT FOR UPDATE`. When the
recruitment transaction begins, the study row is locked at the database level.
Any concurrent transaction attempting to lock the same row will block until the
first transaction commits or rolls back. The second transaction then reads the
updated enrollment count, finds the study full, and returns a 409.

Pessimistic locking was chosen over optimistic locking for a specific reason.
Optimistic locking requires the application to catch `OptimisticLockException`
and implement retry logic. In a clinical context, a retry could mask the fact
that the slot was taken — the application would silently retry, find the study
full, and return an error, but the developer experience of debugging retry
chains is complex and error-prone. Pessimistic locking fails fast and
explicitly: one transaction wins, one loses, and the loser gets a clear 409
with no ambiguity about what happened.

The trade-off is that pessimistic locking slightly reduces throughput under
high concurrency because threads block. For a clinical trial platform where
recruitment events are infrequent and correctness is paramount, this is
entirely acceptable.

---

## 7. Testing Strategy

The testing approach follows the standard pyramid: more unit tests at the base,
fewer integration tests above, and no end-to-end tests for this assessment
scope.

### Service Layer — Unit Tests

Service tests use Mockito to mock the repository layer. This keeps tests fast,
isolated, and independent of the database. Every recruitment guard has its own
test case. Every lifecycle transition — valid and invalid — is tested. Every
eligibility rule is tested with boundary conditions.

The key edge cases covered are: over-enrollment when study is at capacity,
invalid state transitions such as attempting to move a CLOSED study back to
OPEN, duplicate enrollment when a patient is already in a study, ineligible
patient recruitment when age or condition does not match, and the concurrent
last-slot scenario.

### Controller Layer — Integration Tests

Controller tests use MockMvc with a full Spring application context. They test
the HTTP contract: correct status codes, correct response body shape, correct
error envelope format, and correct validation rejection of malformed requests.
These tests do not test business logic — that is covered by the service tests.

### Frontend — Component Tests

Frontend tests use Vitest and React Testing Library. The tests verify that
components render the correct data, that form validation fires correctly, and
that mutations are called with the correct arguments. API calls are mocked
using Vitest's module mocking — no real HTTP requests are made in tests.

---

## 8. Design Decisions

### Why Spring Boot

Spring Boot is the assessment's preferred framework and the right choice for
this domain. It provides mature JPA support for the data layer, robust
validation with Bean Validation, clean MVC for the REST layer, and an
excellent testing ecosystem with MockMvc and Spring Boot Test.

### Why React with TypeScript

TypeScript catches an entire class of bugs at compile time — incorrect prop
types, missing fields in API responses, incorrect function signatures. For a
data-driven application with complex domain types like StudyStatus and
enrollment constraints, TypeScript's type system is a significant safety net.

### Why H2

H2 requires zero external infrastructure. The assessor can clone the repository
and run the application with a single command. The `data.sql` seed file ensures
the UI is populated with realistic data immediately. The switch to PostgreSQL
for production requires only a dependency change and environment variables —
no code changes.

### Why a Dedicated Recruitment Endpoint

Recruitment could have been implemented as a PATCH on the patient resource.
Instead it has its own endpoint at `/api/recruitment`. This is because
recruitment is a command — an action with complex domain rules and side effects
— not a simple property update. A dedicated endpoint makes the intent explicit,
keeps the controller thin, and allows the `RecruitmentService` to be tested
in complete isolation from the patient and study services.

### Why Pessimistic Locking

Covered in detail in the Concurrency section. The short answer: in a clinical
context, correctness outweighs throughput.

---

## 9. Trade-offs and Assumptions

**H2 in-memory database.** Data is lost on every restart. This is acceptable
for an assessment where the seed data provides a consistent starting point.
In production, H2 would be replaced with a persistent database such as
PostgreSQL on Render's managed database tier.

**String-based eligibility criteria.** The format `age>18,condition=NSCLC` is
simple to parse and sufficient to demonstrate the concept. It is not flexible
enough for real clinical eligibility rules, which can involve complex boolean
logic, date ranges, and multiple conditions. A production system would use a
structured rules engine or JSON-based rule definitions.

**No authentication or authorisation.** Any user can perform any action. In a
real system, recruitment actions would require the researcher role, and study
deletion would require the admin role. Spring Security with JWT would be the
natural addition.

**Seed data on every restart.** The `data.sql` file populates studies and
patients on every application startup. This means any data created through the
UI is lost when the application restarts. This is intentional for the
assessment — the assessor always sees a populated, consistent UI.

**Single-module Maven project.** A production system would separate concerns
into Maven modules: a domain module with no dependencies, an application module
with business logic, and an infrastructure module with JPA and HTTP. The
single-module approach is simpler for an assessment and does not affect code
quality.

**No soft deletes.** Deleting a patient or study is permanent. A production
clinical system would archive records rather than delete them, to maintain
audit trails and regulatory compliance.

---

## 10. What I Would Improve With More Time

**PostgreSQL on Render.** Replace H2 with a persistent managed database.
The code is already structured to make this a configuration-only change.

**Spring Security with JWT.** Add role-based access control. Researchers can
recruit patients and view data. Admins can manage studies and delete records.
This is the most important missing feature for a real clinical system.

**WebSocket for live updates.** When one clinician recruits a patient, other
clinicians' browsers update automatically without a page refresh. Spring
provides WebSocket support via STOMP, and React Query can be invalidated from
a WebSocket message handler.

**Advanced eligibility engine.** Replace the string parser with a JSON-based
rules engine that supports boolean logic, range checks, and multiple conditions.
This would allow non-technical staff to define eligibility rules through a UI.

**Soft deletes.** Archive patients and studies rather than permanently deleting
them. This maintains a complete audit trail, which is a regulatory requirement
in real clinical trial systems.

**OpenAPI documentation.** Add springdoc-openapi to auto-generate interactive
API documentation. This would allow the frontend team and any third-party
integrators to explore the API without reading the source code.

**Audit trail.** Record who recruited which patient, when, and from which IP
address. In a regulated clinical environment, every state change must be
traceable to a specific user.

**Contract tests.** Add Pact consumer-driven contract tests between the
frontend and backend. This would catch API breaking changes before they reach
production, which becomes critical when the frontend and backend teams work
independently.

**Multi-module Maven structure.** Split the backend into domain, application,
and infrastructure modules with explicit dependency rules. This enforces
architectural boundaries at the build level and prevents accidental coupling
between layers.

---

## 11. Bonus Features Implemented

The following optional bonus features from the assessment were implemented:

**Docker setup.** The backend has a multi-stage Dockerfile that builds the JAR
in a Maven build stage and runs it in a minimal JRE image. This keeps the
production image small and is what Render uses to deploy the backend.

**CI pipeline.** GitHub Actions workflows run on every push. The backend
workflow runs all 56 tests and builds the Docker image. The frontend workflow
runs all 9 component tests and produces a production Vite build. Workflows are
path-scoped — a frontend change never triggers a backend build.

**Domain events.** `PatientRecruitedEvent` is emitted on every successful
recruitment. The architecture supports swapping the logging handler for a
real message broker without changing the service layer.
