package io.bookwright.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

public class BookingRowMapper implements RowMapper<BookingRow> {

    @Override
    public BookingRow map(ResultSet rs, StatementContext ctx) throws SQLException {
        return BookingRow.builder()
                .id(rs.getInt("id"))
                .roomId(rs.getInt("room_id"))
                .guestFirstName(rs.getString("guest_first_name"))
                .guestLastName(rs.getString("guest_last_name"))
                .checkin(rs.getDate("checkin").toLocalDate())
                .checkout(rs.getDate("checkout").toLocalDate())
                .depositPaid(rs.getBoolean("deposit_paid"))
                .build();
    }
}
