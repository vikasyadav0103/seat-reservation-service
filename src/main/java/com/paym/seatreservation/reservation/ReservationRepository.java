package com.paym.seatreservation.reservation;

import com.paym.seatreservation.show.domain.ReservationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

interface ReservationRepository extends JpaRepository<ReservationEntity, Long> {
}
