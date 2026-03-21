package dev.svenehrke.demo.outbound.db;

import dev.svenehrke.demo.core.PeopleRepository;
import dev.svenehrke.demo.inbound.web.*;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.svenehrke.demo.inbound.web.PersonDetailModel;
import org.svenehrke.demo.inbound.web.PersonEditModel;
import org.svenehrke.demo.inbound.web.PersonTableModel;
import org.svenehrke.demo.inbound.web.PersonTableRowModel;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class H2PeopleRepository implements PeopleRepository {

	@Inject
	DataSource dataSource;

	@Override
	public PersonTableModel people() {
		String sql = "SELECT id, firstname, lastname, streetname FROM Person LIMIT 20";
		List<PersonTableRowModel> rows = new ArrayList<>();
		int total = 0;

		try (Connection conn = dataSource.getConnection()) {

			// Fetch rows
			try (PreparedStatement stmt = conn.prepareStatement(sql);
				 ResultSet rs = stmt.executeQuery()) {
				while (rs.next()) {
					rows.add(new PersonTableRowModel(
						rs.getInt("id"),
						rs.getString("firstname"),
						rs.getString("lastname"),
						rs.getString("streetname")
					));
				}
			}

			// Fetch total
			try (PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) FROM Person");
				 ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) {
					total = rs.getInt(1);
				}
			}

		} catch (SQLException e) {
			throw new RuntimeException(e);
		}

		return new PersonTableModel(rows, total);
	}

	@Override
	public PersonTableModel peopleForSearch(String search) {
		String sql = """
                SELECT id, firstname, lastname, streetname
                FROM Person
                WHERE firstname LIKE ? OR lastname LIKE ? OR streetname LIKE ?
                LIMIT 20
                """;
		List<PersonTableRowModel> rows = new ArrayList<>();
		int total = 0;

		try (Connection conn = dataSource.getConnection()) {

			// Fetch rows with search
			try (PreparedStatement stmt = conn.prepareStatement(sql)) {
				String param = "%" + search + "%";
				stmt.setString(1, param);
				stmt.setString(2, param);
				stmt.setString(3, param);
				try (ResultSet rs = stmt.executeQuery()) {
					while (rs.next()) {
						rows.add(new PersonTableRowModel(
							rs.getInt("id"),
							rs.getString("firstname"),
							rs.getString("lastname"),
							rs.getString("streetname")
						));
					}
				}
			}

			// Fetch total for full table (optional: could count search matches if needed)
			try (PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) FROM Person");
				 ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) {
					total = rs.getInt(1);
				}
			}

		} catch (SQLException e) {
			throw new RuntimeException(e);
		}

		return new PersonTableModel(rows, total);
	}

	@Override
	public int total() {
		try (Connection conn = dataSource.getConnection();
			 PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) FROM Person");
			 ResultSet rs = stmt.executeQuery()) {
			if (rs.next()) {
				return rs.getInt(1);
			}
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		return 0;
	}

	@Override
	public PersonTableRowModel personTableRowModel(int id) {
		String sql = "SELECT id, firstname, lastname, streetname FROM Person WHERE id = ?";
		try (Connection conn = dataSource.getConnection();
			 PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setInt(1, id);
			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) {
					return new PersonTableRowModel(
						rs.getInt("id"),
						rs.getString("firstname"),
						rs.getString("lastname"),
						rs.getString("streetname")
					);
				}
			}
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		return null;
	}

	@Override
	public PersonEditModel personEditModel(int id) {
		String sql = "SELECT id, firstname, lastname, streetname FROM Person WHERE id = ?";
		try (Connection conn = dataSource.getConnection();
			 PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setInt(1, id);
			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) {
					return new PersonEditModel(
						rs.getInt("id"),
						rs.getString("firstname"),
						rs.getString("lastname"),
						rs.getString("streetname")
					);
				}
			}
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		return null;
	}

	@Override
	public PersonDetailModel personDetailModel(int id) {
		String sql = """
                SELECT id, firstname, lastname, streetname, streetno, zipcode, city,
                       country, mailbox, phonenumber, cellphone
                FROM Person
                WHERE id = ?
                """;
		try (Connection conn = dataSource.getConnection();
			 PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setInt(1, id);
			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) {
					return new PersonDetailModel(
						rs.getInt("id"),
						rs.getString("firstname"),
						rs.getString("lastname"),
						rs.getString("streetname"),
						rs.getString("streetno"),
						rs.getString("zipcode"),
						rs.getString("city"),
						rs.getString("country"),
						rs.getString("mailbox"),
						rs.getString("phonenumber"),
						rs.getString("cellphone")
					);
				}
			}
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		return null;
	}

	@Override
	public int deleteByIds(List<Integer> ids) {
		if (ids.isEmpty()) return 0;
		StringBuilder sql = new StringBuilder("DELETE FROM Person WHERE id IN (");
		for (int i = 0; i < ids.size(); i++) {
			sql.append("?");
			if (i < ids.size() - 1) sql.append(",");
		}
		sql.append(")");
		try (Connection conn = dataSource.getConnection();
			 PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
			for (int i = 0; i < ids.size(); i++) {
				stmt.setInt(i + 1, ids.get(i));
			}
			return stmt.executeUpdate();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public int updatePerson(int id, PersonEditModel personEditModel) {
		String sql = """
                UPDATE Person
                SET firstname = ?, lastname = ?, streetname = ?
                WHERE id = ?
                """;
		try (Connection conn = dataSource.getConnection();
			 PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setString(1, personEditModel.firstName());
			stmt.setString(2, personEditModel.lastName());
			stmt.setString(3, personEditModel.streetName());
			stmt.setInt(4, id);
			return stmt.executeUpdate();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}
}
