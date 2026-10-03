# Local JMeter test

The test plan `seat-reservation-local.jmx` targets the local Docker Compose application at `127.0.0.1:8080`.

It creates a show, reads its state, reserves a seat, and cancels the reservation. Tokens are supplied at runtime and are never stored in the plan.

Run one local smoke iteration from the repository root:

```text
jmeter -n -t performance/seat-reservation-local.jmx -l performance/results.jtl -JADMIN_TOKEN=<admin-jwt> -JUSER_TOKEN=<user-jwt>
```

For contention testing, use multiple threads and loops:

```text
jmeter -n -t performance/seat-reservation-local.jmx -l performance/contention.jtl -JADMIN_TOKEN=<admin-jwt> -JUSER_TOKEN=<user-jwt> -JTHREADS=20 -JLOOPS=10 -JRAMP_SECONDS=5
```

Do not commit `.jtl` result files or JWTs. The plan uses a newly created show per thread, so it is safe for local smoke testing. A separate hot-seat plan should use one pre-created show when measuring conflict behavior at scale.

## Hot-seat contention test

Create one local show with a single `A1` seat, note its ID, then run:

```text
jmeter -n -t performance/seat-reservation-hot-seat.jmx -l performance/hot-seat-results.jtl -JSHOW_ID=<show-id> -JUSER_TOKEN=<user-jwt> -JTHREADS=50 -JLOOPS=1 -JRAMP_SECONDS=1
```

The contention report treats `201 Created` and expected `409 Conflict` responses as successful test outcomes. The invariant is exactly one `201` for seat `A1`, with every other attempt returning `409` and no `5xx` responses.
