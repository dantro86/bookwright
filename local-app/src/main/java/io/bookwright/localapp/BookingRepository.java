package io.bookwright.localapp;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.sql.DataSource;

final class BookingRepository {

  private final DataSource dataSource;

  BookingRepository(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  boolean isReady() {
    try (Connection connection = dataSource.getConnection();
        PreparedStatement statement = connection.prepareStatement("SELECT 1");
        ResultSet result = statement.executeQuery()) {
      return result.next() && result.getInt(1) == 1;
    } catch (SQLException exception) {
      return false;
    }
  }

  BookingResponse create(BookingRequest booking) throws SQLException {
    String sql =
        """
        INSERT INTO booking
          (room_id, guest_first_name, guest_last_name, checkin, checkout, deposit_paid)
        VALUES (?, ?, ?, ?, ?, ?)
        """;
    try (Connection connection = dataSource.getConnection();
        PreparedStatement statement =
            connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
      statement.setInt(1, booking.roomId());
      statement.setString(2, booking.guestFirstName());
      statement.setString(3, booking.guestLastName());
      statement.setDate(4, Date.valueOf(booking.checkin()));
      statement.setDate(5, Date.valueOf(booking.checkout()));
      statement.setBoolean(6, booking.depositPaid());
      statement.executeUpdate();
      try (ResultSet keys = statement.getGeneratedKeys()) {
        if (!keys.next()) {
          throw new SQLException("MySQL did not return a generated booking id");
        }
        return find(keys.getInt(1));
      }
    }
  }

  BookingResponse find(int id) throws SQLException {
    String sql = "SELECT * FROM booking WHERE id = ?";
    try (Connection connection = dataSource.getConnection();
        PreparedStatement statement = connection.prepareStatement(sql)) {
      statement.setInt(1, id);
      try (ResultSet result = statement.executeQuery()) {
        return result.next() ? map(result) : null;
      }
    }
  }

  boolean delete(int id) throws SQLException {
    try (Connection connection = dataSource.getConnection();
        PreparedStatement statement =
            connection.prepareStatement("DELETE FROM booking WHERE id = ?")) {
      statement.setInt(1, id);
      return statement.executeUpdate() == 1;
    }
  }

  private BookingResponse map(ResultSet result) throws SQLException {
    return new BookingResponse(
        result.getInt("id"),
        result.getInt("room_id"),
        result.getString("guest_first_name"),
        result.getString("guest_last_name"),
        result.getDate("checkin").toLocalDate(),
        result.getDate("checkout").toLocalDate(),
        result.getBoolean("deposit_paid"));
  }
}
