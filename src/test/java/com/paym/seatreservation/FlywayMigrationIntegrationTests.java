package com.paym.seatreservation;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class FlywayMigrationIntegrationTests {

	@Autowired
	private DataSource dataSource;

	@Test
	void appliesInitialReservationSchema() throws Exception {
		try (Connection connection = dataSource.getConnection()) {
			DatabaseMetaData metadata = connection.getMetaData();
			try (ResultSet tables = metadata.getTables(null, null, "shows", new String[] { "TABLE" })) {
				assertTrue(tables.next());
			}
		}
	}

}
