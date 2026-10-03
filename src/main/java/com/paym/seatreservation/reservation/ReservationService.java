package com.paym.seatreservation.reservation;

import com.paym.seatreservation.reservation.api.CreateReservationRequest;
import com.paym.seatreservation.reservation.api.ReservationResponse;
import com.paym.seatreservation.reservation.domain.IdempotencyKeyEntity;
import com.paym.seatreservation.reservation.domain.UserShowLimitEntity;
import com.paym.seatreservation.show.SeatRepository;
import com.paym.seatreservation.show.ShowNotFoundException;
import com.paym.seatreservation.show.ShowRepository;
import com.paym.seatreservation.show.domain.ReservationEntity;
import com.paym.seatreservation.show.domain.SeatEntity;
import com.paym.seatreservation.show.domain.SeatStatus;
import com.paym.seatreservation.show.domain.ShowEntity;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReservationService {

	private final IdempotencyKeyRepository idempotencyKeyRepository;
	private final ReservationRepository reservationRepository;
	private final SeatRepository seatRepository;
	private final ShowRepository showRepository;
	private final UserShowLimitRepository userShowLimitRepository;

	ReservationService(
		IdempotencyKeyRepository idempotencyKeyRepository,
		ReservationRepository reservationRepository,
		SeatRepository seatRepository,
		ShowRepository showRepository,
		UserShowLimitRepository userShowLimitRepository
	) {
		this.idempotencyKeyRepository = idempotencyKeyRepository;
		this.reservationRepository = reservationRepository;
		this.seatRepository = seatRepository;
		this.showRepository = showRepository;
		this.userShowLimitRepository = userShowLimitRepository;
	}

	@Transactional
	public ReservationResponse reserve(long showId, String userId, CreateReservationRequest request) {
		List<String> seatNumbers = normalizedSeatNumbers(request.seats());
		String requestHash = requestHash(seatNumbers);
		String idempotencyKey = request.idempotency_key().trim();

		idempotencyKeyRepository.insertIfAbsent(userId, showId, idempotencyKey, requestHash);
		IdempotencyKeyEntity idempotencyKeyRecord = idempotencyKeyRepository.findForUpdate(userId, showId, idempotencyKey)
			.orElseThrow(() -> new IllegalStateException("Idempotency key was not persisted"));
		if (!idempotencyKeyRecord.getRequestHash().equals(requestHash)) {
			throw new ReservationConflictException("Idempotency key was already used for a different request");
		}
		if (idempotencyKeyRecord.getReservation() != null) {
			return toResponse(idempotencyKeyRecord.getReservation());
		}

		ShowEntity show = showRepository.findById(showId)
			.orElseThrow(() -> new ShowNotFoundException(showId));
		userShowLimitRepository.insertIfAbsent(showId, userId);
		UserShowLimitEntity userShowLimit = userShowLimitRepository.findForUpdate(showId, userId)
			.orElseThrow(() -> new ShowNotFoundException(showId));

		List<SeatEntity> seats = seatRepository.findForUpdateByShowIdAndSeatNumbers(showId, seatNumbers);
		if (seats.size() != seatNumbers.size()) {
			throw new ReservationConflictException("One or more requested seats do not exist for this show");
		}
		if (seats.stream().anyMatch(seat -> seat.getStatus() != SeatStatus.AVAILABLE)) {
			throw new ReservationConflictException("One or more requested seats are unavailable");
		}
		if (!userShowLimit.canReserve(seats.size())) {
			throw new ReservationConflictException("Per-user seat limit would be exceeded");
		}

		long amountPaise = Math.multiplyExact(show.getPricePaise(), seats.size());
		ReservationEntity reservation = new ReservationEntity(show, userId, amountPaise);
		seats.forEach(reservation::addSeat);
		reservationRepository.saveAndFlush(reservation);
		seats.forEach(seat -> seat.confirm(reservation));
		userShowLimit.reserve(seats.size());
		idempotencyKeyRecord.assignReservation(reservation);

		return toResponse(reservation);
	}

	private List<String> normalizedSeatNumbers(List<String> requestedSeatNumbers) {
		Set<String> uniqueSeatNumbers = new HashSet<>();
		for (String requestedSeatNumber : requestedSeatNumbers) {
			String seatNumber = requestedSeatNumber.trim();
			if (!uniqueSeatNumbers.add(seatNumber)) {
				throw new ReservationConflictException("Seat number %s was requested more than once".formatted(seatNumber));
			}
		}
		return uniqueSeatNumbers.stream().sorted().toList();
	}

	private String requestHash(List<String> seatNumbers) {
		try {
			byte[] digest = MessageDigest.getInstance("SHA-256")
				.digest(String.join("\n", seatNumbers).getBytes(StandardCharsets.UTF_8));
			return java.util.HexFormat.of().formatHex(digest);
		} catch (NoSuchAlgorithmException exception) {
			throw new IllegalStateException("SHA-256 is not available", exception);
		}
	}

	private ReservationResponse toResponse(ReservationEntity reservation) {
		List<String> seats = reservation.getSeats().stream()
			.map(SeatEntity::getSeatNumber)
			.sorted(Comparator.naturalOrder())
			.toList();
		return new ReservationResponse(
			reservation.getId(), reservation.getShowId(), reservation.getUserId(), seats,
			reservation.getAmountPaise(), reservation.getStatus().name()
		);
	}
}
