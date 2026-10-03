package com.paym.seatreservation.show;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.paym.seatreservation.TestcontainersConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@ActiveProfiles("test")
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@SpringBootTest
class ShowApiIntegrationTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@BeforeEach
	void clearDatabase() {
		jdbcTemplate.execute("TRUNCATE TABLE shows CASCADE");
	}

	@Test
	void adminCanCreateShowWithAvailableSeats() throws Exception {
		mockMvc.perform(post("/api/v1/shows")
				.with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{"name":"Friday Night","seats":["A1","A2"],"price_paise":25000}
					"""))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.name").value("Friday Night"))
			.andExpect(jsonPath("$.price_paise").value(25000))
			.andExpect(jsonPath("$.per_user_limit").value(4))
			.andExpect(jsonPath("$.total_seats").value(2))
			.andExpect(jsonPath("$.available").value(2))
			.andExpect(jsonPath("$.seats[0].seat_number").value("A1"))
			.andExpect(jsonPath("$.seats[0].status").value("AVAILABLE"));
	}

	@Test
	void rejectsUnauthenticatedShowCreation() throws Exception {
		mockMvc.perform(post("/api/v1/shows")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{"name":"Friday Night","seats":["A1"],"price_paise":25000}
					"""))
			.andExpect(status().isUnauthorized());
	}

	@Test
	void rejectsShowCreationByNonAdmin() throws Exception {
		mockMvc.perform(post("/api/v1/shows")
				.with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER")))
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{"name":"Friday Night","seats":["A1"],"price_paise":25000}
					"""))
			.andExpect(status().isForbidden());
	}

	@Test
	void rejectsDuplicateSeatsAndInvalidPrices() throws Exception {
		mockMvc.perform(post("/api/v1/shows")
				.with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{"name":"Friday Night","seats":["A1","A1"],"price_paise":25000}
					"""))
			.andExpect(status().isBadRequest());

		mockMvc.perform(post("/api/v1/shows")
				.with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{"name":"Friday Night","seats":["A1"],"price_paise":-1}
					"""))
			.andExpect(status().isBadRequest());
	}

	@Test
	void authenticatedUserCanReadReconciledShowState() throws Exception {
		MvcResult creation = mockMvc.perform(post("/api/v1/shows")
				.with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{"name":"Friday Night","seats":["B2","A1","A2"],"price_paise":25000}
					"""))
			.andExpect(status().isCreated())
			.andReturn();

		String location = creation.getResponse().getHeader("Location");
		mockMvc.perform(get(location)
				.with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER"))))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.total_seats").value(3))
			.andExpect(jsonPath("$.available").value(3))
			.andExpect(jsonPath("$.held").value(0))
			.andExpect(jsonPath("$.confirmed").value(0))
			.andExpect(jsonPath("$.seats[0].seat_number").value("A1"))
			.andExpect(jsonPath("$.seats[2].seat_number").value("B2"));
	}

	@Test
	void returnsNotFoundForMissingShow() throws Exception {
		mockMvc.perform(get("/api/v1/shows/999")
				.with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER"))))
			.andExpect(status().isNotFound());
	}
}
