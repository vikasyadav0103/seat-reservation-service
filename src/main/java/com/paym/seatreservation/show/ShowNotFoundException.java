package com.paym.seatreservation.show;

public class ShowNotFoundException extends RuntimeException {

	public ShowNotFoundException(long showId) {
		super("Show %d was not found".formatted(showId));
	}
}
