package com.paym.seatreservation.reservation.api;

import com.paym.seatreservation.reservation.ReservationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/reservations")
public class ReservationCancellationController {

	private final ReservationService reservationService;

	public ReservationCancellationController(ReservationService reservationService) {
		this.reservationService = reservationService;
	}

	@PostMapping("/{reservationId}/cancel")
	public ResponseEntity<ReservationResponse> cancel(
		@PathVariable long reservationId,
		@AuthenticationPrincipal Jwt jwt
	) {
		return ResponseEntity.ok(reservationService.cancel(reservationId, jwt.getSubject()));
	}
}
