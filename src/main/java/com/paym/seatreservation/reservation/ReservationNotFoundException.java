package com.paym.seatreservation.reservation;

public class ReservationNotFoundException extends RuntimeException {

	public ReservationNotFoundException(long reservationId) {
		super("Reservation %d was not found".formatted(reservationId));
	}
}
