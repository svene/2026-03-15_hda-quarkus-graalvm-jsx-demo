package dev.svenehrke.demo.outbound.db;

import io.quarkus.runtime.Startup;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Random;

import net.datafaker.Faker;

@ApplicationScoped
@Startup
public class DBInitializer {

	@Inject
	DataSource dataSource;

	@PostConstruct
	public void init() {
		System.out.println("DBInitializer.init");

		try (Connection conn = dataSource.getConnection()) {

			// Check if the table already has data
			Integer count = 0;
			try (PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) FROM Person");
				 ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) {
					count = rs.getInt(1);
				}
			}

			if (count > 0) {
				return; // already populated
			}

			System.out.println("loading initial data...");
			Faker faker = new Faker(new Random(0));

			String sql = """
                    INSERT INTO Person(firstname, lastname,
                                       streetname, streetno, zipcode, city, country,
                                       mailbox, phonenumber, cellphone)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                    """;

			try (PreparedStatement stmt = conn.prepareStatement(sql)) {
				for (int i = 0; i < 150; i++) {
					var name = faker.name();
					var address = faker.address();
					var phone = faker.phoneNumber();

					stmt.setString(1, name.firstName());
					stmt.setString(2, name.lastName());
					stmt.setString(3, address.streetName());
					stmt.setString(4, address.streetAddressNumber());
					stmt.setString(5, address.zipCode());
					stmt.setString(6, address.city());
					stmt.setString(7, address.country());
					stmt.setString(8, address.mailBox());
					stmt.setString(9, phone.phoneNumber());
					stmt.setString(10, phone.cellPhone());
					stmt.addBatch();
				}
				stmt.executeBatch();
			}

		} catch (SQLException e) {
			throw new RuntimeException("Failed to initialize DB", e);
		}
	}
}
