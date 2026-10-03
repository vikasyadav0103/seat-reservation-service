package com.paym.seatreservation.show.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "seats")
public class SeatEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(optional = false)
	@JoinColumn(name = "show_id", nullable = false)
	private ShowEntity show;

	@ManyToOne
	@JoinColumn(name = "reservation_id")
	private ReservationEntity reservation;

	@Column(name = "seat_number", nullable = false)
	private String seatNumber;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private SeatStatus status;

	protected SeatEntity() {
	}

	SeatEntity(ShowEntity show, String seatNumber) {
		this.show = show;
		this.seatNumber = seatNumber;
		this.status = SeatStatus.AVAILABLE;
	}

	public String getSeatNumber() {
		return seatNumber;
	}

	public SeatStatus getStatus() {
		return status;
	}

	public void confirm(ReservationEntity reservation) {
		this.reservation = reservation;
		this.status = SeatStatus.CONFIRMED;
	}
}
