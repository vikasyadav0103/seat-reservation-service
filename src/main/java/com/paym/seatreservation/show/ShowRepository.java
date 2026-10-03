package com.paym.seatreservation.show;

import com.paym.seatreservation.show.domain.ShowEntity;
import org.springframework.data.jpa.repository.JpaRepository;

interface ShowRepository extends JpaRepository<ShowEntity, Long> {
}
