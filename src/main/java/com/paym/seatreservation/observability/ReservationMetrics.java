package com.paym.seatreservation.observability;

import com.paym.seatreservation.show.ShowRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import org.springframework.stereotype.Component;

@Component
public class ReservationMetrics {

	private final Counter confirmed;
	private final Counter seatTaken;
	private final Counter perUserLimit;
	private final Counter idempotentReplay;

	public ReservationMetrics(MeterRegistry registry, ShowRepository showRepository) {
		confirmed = Counter.builder("reservations_confirmed_total").description("Confirmed reservations").register(registry);
		seatTaken = declined(registry, "seat-taken");
		perUserLimit = declined(registry, "per-user-limit");
		idempotentReplay = declined(registry, "idempotent-replay");
		registry.gauge("seats_available", Tags.empty(), showRepository, ShowRepository::countAvailableSeats);
	}

	public void confirmed() { confirmed.increment(); }
	public void seatTaken() { seatTaken.increment(); }
	public void perUserLimit() { perUserLimit.increment(); }
	public void idempotentReplay() { idempotentReplay.increment(); }

	private Counter declined(MeterRegistry registry, String reason) {
		return Counter.builder("reservations_declined_total")
			.description("Declined reservation attempts by reason")
			.tags("reason", reason)
			.register(registry);
	}
}
