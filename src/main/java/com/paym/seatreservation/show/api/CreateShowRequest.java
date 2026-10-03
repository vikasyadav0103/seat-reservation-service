package com.paym.seatreservation.show.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.util.List;

public record CreateShowRequest(
	@NotBlank @Size(max = 255) String name,
	@NotEmpty List<@NotBlank @Size(max = 64) String> seats,
	@NotNull @PositiveOrZero Long price_paise
) {
}
