package com.paym.seatreservation.reservation.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;

public record CreateReservationRequest(
	@NotEmpty List<@NotBlank @Size(max = 64) String> seats,
	@NotBlank @Size(max = 255) String idempotency_key
) {
}
