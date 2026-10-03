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
