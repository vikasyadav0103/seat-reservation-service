package com.paym.seatreservation.reservation.api;

import com.paym.seatreservation.reservation.ReservationService;
import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/shows/{showId}/reservations")
public class ReservationController {

	private final ReservationService reservationService;

	public ReservationController(ReservationService reservationService) {
		this.reservationService = reservationService;
	}

	@PostMapping
	public ResponseEntity<ReservationResponse> reserve(
		@PathVariable long showId,
		@AuthenticationPrincipal Jwt jwt,
		@Valid @RequestBody CreateReservationRequest request
	) {
		ReservationResponse response = reservationService.reserve(showId, jwt.getSubject(), request);
		return ResponseEntity.created(URI.create("/api/v1/reservations/%d".formatted(response.reservation_id())))
			.body(response);
	}

}
