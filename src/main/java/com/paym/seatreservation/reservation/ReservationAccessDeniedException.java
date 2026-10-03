package com.paym.seatreservation.reservation;

public class ReservationAccessDeniedException extends RuntimeException {

	public ReservationAccessDeniedException() {
		super("Only the reservation owner can cancel this reservation");
	}
}
