package com.paym.seatreservation.show.api;

import com.paym.seatreservation.show.ShowService;
import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/shows")
public class ShowController {

	private final ShowService showService;

	public ShowController(ShowService showService) {
		this.showService = showService;
	}

	@PostMapping
	public ResponseEntity<ShowResponse> create(@Valid @RequestBody CreateShowRequest request) {
		ShowResponse response = showService.create(request);
		return ResponseEntity.created(URI.create("/api/v1/shows/%d".formatted(response.id()))).body(response);
	}

	@GetMapping("/{showId}")
	public ShowResponse get(@PathVariable long showId) {
		return showService.get(showId);
	}
}
