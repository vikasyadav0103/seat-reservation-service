package com.paym.seatreservation.reservation;

import com.paym.seatreservation.show.domain.ReservationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface ReservationRepository extends JpaRepository<ReservationEntity, Long> {

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select reservation from ReservationEntity reservation where reservation.id = :reservationId")
	Optional<ReservationEntity> findForUpdate(@Param("reservationId") long reservationId);
}
