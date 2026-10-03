# seat-reservation-service

## Deployment and burst verification

The service is deployed at `https://seat-reservation-service-gnm0.onrender.com`.

Operational endpoints:

- `GET /actuator/health` exposes liveness/readiness; readiness includes the PostgreSQL health indicator.
- `GET /actuator/prometheus` exposes `reservations_confirmed_total`, `reservations_declined_total{reason=...}`, and `seats_available` alongside standard JVM, HTTP, and datasource metrics.
- Every request receives or propagates `X-Correlation-Id`; reservation outcomes are logged with structured key/value fields.

Run a reproducible hot-seat burst from PowerShell. Tokens are supplied at runtime and are never stored in the repository:

```powershell
.\scripts\burst.ps1 -BaseUrl https://seat-reservation-service-gnm0.onrender.com -AdminToken $adminJwt -UserToken $userJwt -Threads 50
```

For the assignment's approximately 20,000 concurrent attempts, use a sufficiently sized deployment and load generator:

```powershell
.\scripts\burst.ps1 -BaseUrl https://seat-reservation-service-gnm0.onrender.com -AdminToken $adminJwt -UserToken $userJwt -Threads 20000 -RampSeconds 300
```

The script prints HTTP outcome counts and final seat reconciliation. Render Free passed cold start, Neon connectivity, Flyway, and functional smoke tests, but saturated during the 20,000-client burst with gateway/time-out errors. That capacity result is documented in `WRITEUP.md`; a larger instance or distributed load generator is required for a clean capacity run.

Docker Compose runs only the backend and connects to the existing database configured by `LOCAL_DB_*` or `PROD_DB_*`; it does not create a second PostgreSQL container.

Compose database separation:

- Local Docker PostgreSQL: `docker compose up --build`
- Neon-backed local production check: `docker compose -f docker-compose.prod.yml up --build`

The production Compose file starts only the backend and never starts a local PostgreSQL container.
