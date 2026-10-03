package com.paym.seatreservation.show.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "reservations")
public class ReservationEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(optional = false)
	@JoinColumn(name = "show_id", nullable = false)
	private ShowEntity show;

	@Column(name = "user_id", nullable = false)
	private String userId;

	@Column(name = "amount_paise", nullable = false)
	private long amountPaise;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private ReservationStatus status;

	@ManyToMany
	@JoinTable(
		name = "reservation_seats",
		joinColumns = @JoinColumn(name = "reservation_id"),
		inverseJoinColumns = @JoinColumn(name = "seat_id")
	)
	private Set<SeatEntity> seats = new LinkedHashSet<>();

	protected ReservationEntity() {
	}

	public ReservationEntity(ShowEntity show, String userId, long amountPaise) {
		this.show = show;
		this.userId = userId;
		this.amountPaise = amountPaise;
		this.status = ReservationStatus.CONFIRMED;
	}

	public void addSeat(SeatEntity seat) {
		seats.add(seat);
	}

	public Long getId() {
		return id;
	}

	public Long getShowId() {
		return show.getId();
	}

	public String getUserId() {
		return userId;
	}

	public long getAmountPaise() {
		return amountPaise;
	}

	public ReservationStatus getStatus() {
		return status;
	}

	public Set<SeatEntity> getSeats() {
		return seats;
	}
}
