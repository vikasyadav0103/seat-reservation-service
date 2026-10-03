package com.paym.seatreservation.show.api;

import com.paym.seatreservation.show.DuplicateSeatNumberException;
import com.paym.seatreservation.show.ShowNotFoundException;
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
}
