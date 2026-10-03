package com.paym.seatreservation.reservation.domain;

import com.paym.seatreservation.show.domain.ReservationEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;

import java.sql.Types;

@Entity
@Table(name = "idempotency_keys")
public class IdempotencyKeyEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "user_id", nullable = false)
	private String userId;

	@Column(name = "show_id", nullable = false)
	private long showId;

	@Column(name = "idempotency_key", nullable = false)
	private String idempotencyKey;

	@Column(name = "request_hash", nullable = false, length = 64)
	@JdbcTypeCode(Types.CHAR)
	private String requestHash;

	@ManyToOne
	@JoinColumn(name = "reservation_id")
	private ReservationEntity reservation;

	public String getRequestHash() {
		return requestHash;
	}

	public ReservationEntity getReservation() {
		return reservation;
	}

	public void assignReservation(ReservationEntity reservation) {
		this.reservation = reservation;
	}
}
