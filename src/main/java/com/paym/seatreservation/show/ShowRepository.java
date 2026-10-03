package com.paym.seatreservation.show;

import com.paym.seatreservation.show.domain.ShowEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ShowRepository extends JpaRepository<ShowEntity, Long> {

	@Query(value = "select count(*) from seats where status = 'AVAILABLE'", nativeQuery = true)
	long countAvailableSeats();
}
