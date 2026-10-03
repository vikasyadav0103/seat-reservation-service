package com.paym.seatreservation.reservation;

import com.paym.seatreservation.reservation.domain.IdempotencyKeyEntity;
import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface IdempotencyKeyRepository extends JpaRepository<IdempotencyKeyEntity, Long> {

	@Modifying
	@Query(value = """
		insert into idempotency_keys (user_id, show_id, idempotency_key, request_hash)
		values (:userId, :showId, :idempotencyKey, :requestHash)
		on conflict (user_id, show_id, idempotency_key) do nothing
		""", nativeQuery = true)
	int insertIfAbsent(
		@Param("userId") String userId,
		@Param("showId") long showId,
		@Param("idempotencyKey") String idempotencyKey,
		@Param("requestHash") String requestHash
	);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("""
		select key from IdempotencyKeyEntity key
		left join fetch key.reservation reservation
		left join fetch reservation.seats
		where key.userId = :userId and key.showId = :showId and key.idempotencyKey = :idempotencyKey
		""")
	Optional<IdempotencyKeyEntity> findForUpdate(
		@Param("userId") String userId,
		@Param("showId") long showId,
		@Param("idempotencyKey") String idempotencyKey
	);
}
