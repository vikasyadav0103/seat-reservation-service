# AGENTS.md

## Project

This is a Paytm backend take-home assignment for a seat reservation service.

The assignment evaluates the running service primarily on:
- concurrency correctness
- no double-selling
- idempotency
- per-user reservation limits
- safe cancellation
- reconciliation correctness
- observability
- deployment reliability

## Technology

- Java 21
- Spring Boot 4.1.1
- Maven
- Spring Web MVC
- Spring Data JPA / Hibernate
- PostgreSQL
- Flyway
- Spring Security
- Bean Validation
- Actuator
- Micrometer Prometheus
- JUnit 5
- Testcontainers

## Architecture Constraints

PostgreSQL is the source of truth for reservation state.

Do not introduce Redis, Kafka, MongoDB, or other infrastructure unless explicitly approved.

Reservation correctness is more important than architectural complexity.

Do not use JVM synchronization, in-memory locks, or application-local state to guarantee seat ownership.

The atomic reservation decision must be enforced by PostgreSQL transactions, row locks, constraints, or conditional updates.

## Reservation Rules

- A seat must never be sold twice.
- Concurrent attempts for the same seat must produce exactly one successful reservation.
- Losing concurrent requests should return a conflict response, not a 5xx.
- Multi-seat reservations must be atomic: either all requested seats are reserved or none are.
- Multi-seat seat locking must use deterministic ordering to avoid deadlocks.
- Per-user limits must remain correct under concurrency.
- Idempotency keys must be enforced at the database level.
- Same idempotency key + same request should return the original result.
- Same idempotency key + different request must return a conflict.
- User identity must come from authentication, never from the request body.
- A user may cancel only their own reservation.

## Configuration

Environment-specific configuration must not be hard-coded.

Use environment variables for:
- database connection
- database credentials
- connection pool configuration
- JWT configuration
- application environment

Never commit secrets, passwords, tokens, or private connection strings.

Local development uses the office PostgreSQL database through VPN.

Production will use PostgreSQL hosted separately from the application.

## Development

Prefer simple solutions suitable for a one-day take-home assignment.

Do not add abstractions or dependencies without a concrete requirement.

Use Flyway migrations for database schema changes.

Do not use Hibernate schema generation as the source of truth.

## Testing

Tests must verify:
- basic reservation
- duplicate seat reservation
- concurrent reservation of the same seat
- multi-seat reservation
- per-user limit
- idempotency
- cancellation
- reconciliation invariant

Concurrency correctness is critical.

Use Testcontainers PostgreSQL for integration/concurrency tests where appropriate.

Normal local development does not require Docker.

## API

Business APIs are expected to include:

POST /api/v1/shows
POST /api/v1/shows/{showId}/reservations
POST /api/v1/reservations/{reservationId}/cancel
GET /api/v1/shows/{showId}

Operational endpoints include health and Prometheus metrics.

## Git

Make small, logical commits.

Do not rewrite unrelated files.

Before completing a task:
1. Run relevant tests.
2. Review the git diff.
3. Report any assumptions or unresolved issues.

Do not make architectural changes silently.

## Agent Behavior

Before implementing a substantial feature:
1. Inspect the existing code.
2. Understand the current architecture.
3. State the implementation approach briefly.
4. Implement the smallest appropriate change.
5. Run relevant tests.
6. Review the diff.
7. Report the result.

Do not build the entire application in one step.