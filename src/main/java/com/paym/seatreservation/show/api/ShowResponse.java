package com.paym.seatreservation.show.api;

import java.util.List;

public record ShowResponse(
	Long id,
	String name,
	long price_paise,
	int per_user_limit,
	long total_seats,
	long available,
	long held,
	long confirmed,
	List<SeatResponse> seats
) {
}
