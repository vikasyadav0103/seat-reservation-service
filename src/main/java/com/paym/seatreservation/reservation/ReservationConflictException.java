package com.paym.seatreservation.reservation;

public class ReservationConflictException extends RuntimeException {

	ReservationConflictException(String message) {
		super(message);
	}
}
