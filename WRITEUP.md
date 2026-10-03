# Seat Reservation Service

## API

All business endpoints are under `/api/v1` and require a JWT unless noted.

### Create a show

`POST /api/v1/shows` with the `ADMIN` role.

```json
{"name":"Friday Night","seats":["A1","A2"],"price_paise":25000}
```

### Read show state

`GET /api/v1/shows/{showId}` with the `USER` role. The response includes every seat and the reconciled available, held, and confirmed counts.

### Reserve seats

`POST /api/v1/shows/{showId}/reservations` with the `USER` role.

```json
{"seats":["A1","A2"],"idempotency_key":"order-123"}
```

The authenticated JWT subject is the user identity. A request is atomic across all requested seats. Reusing the same idempotency key and request returns the original reservation; reusing it for a different request returns `409 Conflict`.

### Cancel a reservation

`POST /api/v1/reservations/{reservationId}/cancel` with the `USER` role. Only the reservation owner can cancel. Cancellation releases all seats and decrements the user's reserved-seat counter in the same transaction. Repeating cancellation returns the cancelled reservation.

## Correctness design

- PostgreSQL is the source of truth.
- Reservation operations run in database transactions.
- Requested seats are locked with `SELECT ... FOR UPDATE` in deterministic ID order.
- A per-user/show row is locked while enforcing the reservation limit.
- Idempotency keys have a database uniqueness constraint and request hash.
- No JVM lock or in-memory ownership state is used.
- Show counts are derived from persisted seat statuses.

## Observability

- `GET /actuator/health`
- `GET /actuator/prometheus`
- Readiness explicitly includes the database health indicator and fails closed when PostgreSQL is unavailable.
- `reservations_confirmed_total` counts confirmed reservations.
- `reservations_declined_total{reason="seat-taken|per-user-limit|idempotent-replay"}` counts declined attempts by reason.
- `seats_available` is queried from persisted PostgreSQL seat rows, so it reconciles with API show state.
- `X-Correlation-Id` is propagated on every request and included in log patterns; reservation logs include outcome, reason, show, user, and reservation identifiers without credentials.

## Burst and deployment evidence

The live service is `https://seat-reservation-service-gnm0.onrender.com`. A cold start completed successfully: Spring Boot became healthy, connected to Neon, and Flyway applied schema version 1. The local and deployed smoke flows completed successfully.

The reproducible command is `scripts/burst.ps1`; it creates a fresh one-seat show, runs the hot-seat JMeter plan, prints HTTP distributions, and reads final persisted reconciliation. A local 20,000-request run produced exactly one `201`, 19,999 expected `409` conflicts, and zero errors.

An approximately 20,000-client run against Render Free was also attempted. The application began returning the expected success/conflict outcomes, but the free instance saturated and Render returned gateway errors, connection timeouts, and some 500 responses. This is a hosting-capacity limitation, not evidence of a second seat sale. A larger service instance and/or distributed load generator is required to produce a clean 20,000-client capacity result.

## Trade-offs and next steps

The service favors consistency over availability during a database or network partition: requests fail rather than claim ownership without PostgreSQL confirmation. Holds and expiry are not part of the current API contract; reservations are confirmed atomically or rejected. At 2am, alerts should cover readiness failures, 5xx rate, conflict-rate changes, database pool exhaustion, and divergence between confirmed reservations and available-seat metrics.

## Local setup

Copy `.env.example` to `.env`, set `DB_*` and a JWT secret of at least 32 characters, then run:

```text
./mvnw spring-boot:run
```

Tests use Testcontainers PostgreSQL and require Docker:

```text
./mvnw test
```

## Docker Compose

Set `JWT_SECRET` in `.env` and run:

```text
docker compose up --build
```

The application listens on `http://localhost:8080` and Flyway applies migrations at startup.

## Production deployment

Use the Docker image with a managed PostgreSQL provider such as Neon. Set `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD`, `DB_POOL_MAX_SIZE`, `DB_POOL_MIN_IDLE`, `JWT_SECRET`, and `JWT_EXPIRATION_SECONDS` in the platform's secret configuration. Do not commit a Neon URL or credentials.
