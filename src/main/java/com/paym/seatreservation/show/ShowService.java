package com.paym.seatreservation.show;

import com.paym.seatreservation.show.api.CreateShowRequest;
import com.paym.seatreservation.show.api.SeatResponse;
import com.paym.seatreservation.show.api.ShowResponse;
import com.paym.seatreservation.show.domain.SeatEntity;
import com.paym.seatreservation.show.domain.SeatStatus;
import com.paym.seatreservation.show.domain.ShowEntity;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ShowService {

	private static final int DEFAULT_PER_USER_LIMIT = 4;

	private final ShowRepository showRepository;

	ShowService(ShowRepository showRepository) {
		this.showRepository = showRepository;
	}

	@Transactional
	public ShowResponse create(CreateShowRequest request) {
		ShowEntity show = new ShowEntity(request.name().trim(), request.price_paise(), DEFAULT_PER_USER_LIMIT);
		Set<String> seatNumbers = new HashSet<>();
		for (String requestedSeatNumber : request.seats()) {
			String seatNumber = requestedSeatNumber.trim();
			if (!seatNumbers.add(seatNumber)) {
				throw new DuplicateSeatNumberException(seatNumber);
			}
			show.addSeat(seatNumber);
		}

		return toResponse(showRepository.save(show));
	}

	@Transactional(readOnly = true)
	public ShowResponse get(long showId) {
		ShowEntity show = showRepository.findById(showId)
			.orElseThrow(() -> new ShowNotFoundException(showId));
		return toResponse(show);
	}

	private ShowResponse toResponse(ShowEntity show) {
		List<SeatResponse> seats = show.getSeats().stream()
			.sorted((left, right) -> left.getSeatNumber().compareTo(right.getSeatNumber()))
			.map(this::toSeatResponse)
			.toList();
		long available = seats.stream().filter(seat -> seat.status().equals(SeatStatus.AVAILABLE.name())).count();
		long held = seats.stream().filter(seat -> seat.status().equals(SeatStatus.HELD.name())).count();
		long confirmed = seats.stream().filter(seat -> seat.status().equals(SeatStatus.CONFIRMED.name())).count();

		return new ShowResponse(show.getId(), show.getName(), show.getPricePaise(), show.getPerUserLimit(),
			seats.size(), available, held, confirmed, seats);
	}

	private SeatResponse toSeatResponse(SeatEntity seat) {
		return new SeatResponse(seat.getSeatNumber(), seat.getStatus().name());
	}
}
