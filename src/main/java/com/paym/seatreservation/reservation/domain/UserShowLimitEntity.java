package com.paym.seatreservation.reservation.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "user_show_limits")
@IdClass(UserShowLimitEntity.Id.class)
public class UserShowLimitEntity {

	@jakarta.persistence.Id
	@Column(name = "show_id")
	private long showId;

	@jakarta.persistence.Id
	@Column(name = "user_id")
	private String userId;

	@Column(name = "reserved_count", nullable = false)
	private int reservedCount;

	@Column(name = "reservation_limit", nullable = false)
	private int reservationLimit;

	public void reserve(int seatCount) {
		reservedCount += seatCount;
	}

	public void release(int seatCount) {
		reservedCount = Math.max(0, reservedCount - seatCount);
	}

	public boolean canReserve(int seatCount) {
		return reservedCount + seatCount <= reservationLimit;
	}

	public static class Id implements Serializable {
		private long showId;
		private String userId;

		public Id() {
		}

		@Override
		public boolean equals(Object other) {
			return other instanceof Id id && showId == id.showId && Objects.equals(userId, id.userId);
		}

		@Override
		public int hashCode() {
			return Objects.hash(showId, userId);
		}
	}
}
