package com.paym.seatreservation.show;

public class DuplicateSeatNumberException extends RuntimeException {

	DuplicateSeatNumberException(String seatNumber) {
		super("Seat number %s is duplicated".formatted(seatNumber));
	}
}
