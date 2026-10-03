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
