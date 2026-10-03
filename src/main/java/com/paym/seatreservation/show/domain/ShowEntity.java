package com.paym.seatreservation.show.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "shows")
public class ShowEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String name;

	@Column(name = "price_paise", nullable = false)
	private long pricePaise;

	@Column(name = "per_user_limit", nullable = false)
	private int perUserLimit;

	@OneToMany(mappedBy = "show", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<SeatEntity> seats = new ArrayList<>();

	protected ShowEntity() {
	}

	public ShowEntity(String name, long pricePaise, int perUserLimit) {
		this.name = name;
		this.pricePaise = pricePaise;
		this.perUserLimit = perUserLimit;
	}

	public void addSeat(String seatNumber) {
		seats.add(new SeatEntity(this, seatNumber));
	}

	public Long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public long getPricePaise() {
		return pricePaise;
	}

	public int getPerUserLimit() {
		return perUserLimit;
	}

	public List<SeatEntity> getSeats() {
		return seats;
	}
}
