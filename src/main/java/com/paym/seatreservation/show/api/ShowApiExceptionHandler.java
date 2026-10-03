package com.paym.seatreservation.show.api;

import com.paym.seatreservation.show.DuplicateSeatNumberException;
import com.paym.seatreservation.show.ShowNotFoundException;
import com.paym.seatreservation.reservation.ReservationConflictException;
import com.paym.seatreservation.reservation.ReservationNotFoundException;
import com.paym.seatreservation.reservation.ReservationAccessDeniedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
class ShowApiExceptionHandler {

	@ExceptionHandler(DuplicateSeatNumberException.class)
	ProblemDetail handleDuplicateSeatNumber(DuplicateSeatNumberException exception) {
		ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
		problem.setDetail(exception.getMessage());
		return problem;
	}

	@ExceptionHandler(ShowNotFoundException.class)
	ProblemDetail handleShowNotFound(ShowNotFoundException exception) {
		ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
		problem.setDetail(exception.getMessage());
		return problem;
	}

	@ExceptionHandler(ReservationConflictException.class)
	ProblemDetail handleReservationConflict(ReservationConflictException exception) {
		ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.CONFLICT);
		problem.setDetail(exception.getMessage());
		return problem;
	}

	@ExceptionHandler(ReservationNotFoundException.class)
	ProblemDetail handleReservationNotFound(ReservationNotFoundException exception) {
		ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
		problem.setDetail(exception.getMessage());
		return problem;
	}

	@ExceptionHandler(ReservationAccessDeniedException.class)
	ProblemDetail handleReservationAccessDenied(ReservationAccessDeniedException exception) {
		ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.FORBIDDEN);
		problem.setDetail(exception.getMessage());
		return problem;
	}
}
