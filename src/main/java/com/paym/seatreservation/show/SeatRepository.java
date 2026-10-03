package com.paym.seatreservation.show;

import com.paym.seatreservation.show.domain.SeatEntity;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

public interface SeatRepository extends JpaRepository<SeatEntity, Long> {

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select seat from SeatEntity seat where seat.show.id = :showId and seat.seatNumber in :seatNumbers order by seat.id")
	List<SeatEntity> findForUpdateByShowIdAndSeatNumbers(
		@Param("showId") long showId,
		@Param("seatNumbers") Collection<String> seatNumbers
	);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select seat from SeatEntity seat where seat.id in :seatIds order by seat.id")
	List<SeatEntity> findForUpdateByIds(@Param("seatIds") Collection<Long> seatIds);
}
