package com.paym.seatreservation.reservation;

import com.paym.seatreservation.reservation.domain.UserShowLimitEntity;
import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface UserShowLimitRepository extends JpaRepository<UserShowLimitEntity, UserShowLimitEntity.Id> {

	@Modifying
	@Query(value = """
		insert into user_show_limits (show_id, user_id, reserved_count, reservation_limit)
		select id, :userId, 0, per_user_limit from shows where id = :showId
		on conflict (show_id, user_id) do nothing
		""", nativeQuery = true)
	int insertIfAbsent(@Param("showId") long showId, @Param("userId") String userId);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select limit from UserShowLimitEntity limit where limit.showId = :showId and limit.userId = :userId")
	Optional<UserShowLimitEntity> findForUpdate(@Param("showId") long showId, @Param("userId") String userId);
}
