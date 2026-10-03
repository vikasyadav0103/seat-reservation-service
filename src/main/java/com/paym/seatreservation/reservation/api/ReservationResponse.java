package com.paym.seatreservation.reservation.api;

import java.util.List;

public record ReservationResponse(
	Long reservation_id,
	Long show_id,
	String user_id,
	List<String> seats,
	long amount_paise,
	String status
) {
}
