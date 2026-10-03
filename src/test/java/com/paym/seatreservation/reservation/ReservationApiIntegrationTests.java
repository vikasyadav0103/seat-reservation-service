package com.paym.seatreservation.reservation;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.paym.seatreservation.TestcontainersConfiguration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

@ActiveProfiles("test")
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@SpringBootTest
class ReservationApiIntegrationTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@BeforeEach
	void clearDatabase() {
		jdbcTemplate.execute("TRUNCATE TABLE shows CASCADE");
	}

	@Test
	void reservesOneSeatAndCalculatesIntegerAmount() throws Exception {
		long showId = createShow("A1", "A2");

		mockMvc.perform(post("/api/v1/shows/{showId}/reservations", showId)
				.with(jwtFor("user-1"))
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{"seats":["A1"],"idempotency_key":"key-1"}
					"""))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.show_id").value(showId))
			.andExpect(jsonPath("$.user_id").value("user-1"))
			.andExpect(jsonPath("$.amount_paise").value(25000))
			.andExpect(jsonPath("$.status").value("CONFIRMED"));
	}

	@Test
	void replaysSameIdempotencyKeyAndRejectsDifferentBody() throws Exception {
		long showId = createShow("A1", "A2");
		String request = """
			{"seats":["A1"],"idempotency_key":"same-key"}
			""";

		MvcResult first = mockMvc.perform(post("/api/v1/shows/{showId}/reservations", showId)
				.with(jwtFor("user-1"))
				.contentType(MediaType.APPLICATION_JSON)
				.content(request))
			.andExpect(status().isCreated())
			.andReturn();
		Long firstReservationId = ((Number) com.jayway.jsonpath.JsonPath.read(
			first.getResponse().getContentAsString(), "$.reservation_id")).longValue();

		mockMvc.perform(post("/api/v1/shows/{showId}/reservations", showId)
				.with(jwtFor("user-1"))
				.contentType(MediaType.APPLICATION_JSON)
				.content(request))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.reservation_id").value(firstReservationId));

		mockMvc.perform(post("/api/v1/shows/{showId}/reservations", showId)
				.with(jwtFor("user-1"))
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{"seats":["A2"],"idempotency_key":"same-key"}
					"""))
			.andExpect(status().isConflict());

		org.junit.jupiter.api.Assertions.assertTrue(first.getResponse().getContentAsString().contains("reservation_id"));
	}

	@Test
	void unavailableSeatReturnsConflict() throws Exception {
		long showId = createShow("A1");
		reserve(showId, "user-1", "A1", "first");

		mockMvc.perform(post("/api/v1/shows/{showId}/reservations", showId)
				.with(jwtFor("user-2"))
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{"seats":["A1"],"idempotency_key":"second"}
					"""))
			.andExpect(status().isConflict());
	}

	@Test
	void ownerCanCancelAndSeatBecomesAvailableAgain() throws Exception {
		long showId = createShow("A1");
		long reservationId = reserve(showId, "user-1", "A1", "cancel-key");

		mockMvc.perform(post("/api/v1/reservations/{reservationId}/cancel", reservationId)
				.with(jwtFor("user-1")))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.status").value("CANCELLED"));

		reserve(showId, "user-2", "A1", "after-cancel");
	}

	@Test
	void onlyOwnerCanCancelAndRepeatedCancelIsIdempotent() throws Exception {
		long showId = createShow("A1");
		long reservationId = reserve(showId, "user-1", "A1", "owner-key");

		mockMvc.perform(post("/api/v1/reservations/{reservationId}/cancel", reservationId)
				.with(jwtFor("user-2")))
			.andExpect(status().isForbidden());

		mockMvc.perform(post("/api/v1/reservations/{reservationId}/cancel", reservationId)
				.with(jwtFor("user-1")))
			.andExpect(status().isOk());
		mockMvc.perform(post("/api/v1/reservations/{reservationId}/cancel", reservationId)
				.with(jwtFor("user-1")))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.status").value("CANCELLED"));
	}

	@Test
	void perUserLimitIsEnforced() throws Exception {
		long showId = createShow("A1", "A2", "A3", "A4", "A5");
		reserve(showId, "user-1", "A1", "one");
		reserve(showId, "user-1", "A2", "two");
		reserve(showId, "user-1", "A3", "three");
		reserve(showId, "user-1", "A4", "four");

		mockMvc.perform(post("/api/v1/shows/{showId}/reservations", showId)
				.with(jwtFor("user-1"))
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{"seats":["A5"],"idempotency_key":"five"}
					"""))
			.andExpect(status().isConflict());
	}

	@Test
	void concurrentHotSeatProducesOneWinnerAndNoServerErrors() throws Exception {
		long showId = createShow("A1");
		ExecutorService executor = Executors.newFixedThreadPool(8);
		try {
			List<Callable<Integer>> attempts = new ArrayList<>();
			for (int index = 0; index < 8; index++) {
				int userNumber = index;
				attempts.add(() -> mockMvc.perform(post("/api/v1/shows/{showId}/reservations", showId)
						.with(jwtFor("storm-user-" + userNumber))
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
							{"seats":["A1"],"idempotency_key":"storm-key-%d"}
							""".formatted(userNumber)))
					.andReturn().getResponse().getStatus());
			}

			List<Future<Integer>> results = executor.invokeAll(attempts);
			long winners = 0;
			for (Future<Integer> result : results) {
				int status = result.get();
				if (status == 201) {
					winners++;
				} else {
					org.junit.jupiter.api.Assertions.assertEquals(409, status);
				}
			}
			org.junit.jupiter.api.Assertions.assertEquals(1, winners);
		} finally {
			executor.shutdownNow();
		}
	}

	private long createShow(String... seats) throws Exception {
		String seatJson = java.util.Arrays.stream(seats).map(seat -> "\"" + seat + "\"").collect(java.util.stream.Collectors.joining(","));
		MvcResult result = mockMvc.perform(post("/api/v1/shows")
				.with(jwt().authorities(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_ADMIN")))
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"name\":\"Test Show\",\"seats\":[%s],\"price_paise\":25000}".formatted(seatJson)))
			.andExpect(status().isCreated())
			.andReturn();
		return ((Number) com.jayway.jsonpath.JsonPath.read(result.getResponse().getContentAsString(), "$.id")).longValue();
	}

	private long reserve(long showId, String userId, String seat, String key) throws Exception {
		MvcResult result = mockMvc.perform(post("/api/v1/shows/{showId}/reservations", showId)
				.with(jwtFor(userId))
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"seats\":[\"%s\"],\"idempotency_key\":\"%s\"}".formatted(seat, key)))
			.andExpect(status().isCreated())
			.andReturn();
		return ((Number) com.jayway.jsonpath.JsonPath.read(
			result.getResponse().getContentAsString(), "$.reservation_id")).longValue();
	}

	private RequestPostProcessor jwtFor(String subject) {
		return jwt().jwt(jwt -> jwt.claim(JwtClaimNames.SUB, subject))
			.authorities(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_USER"));
	}
}
