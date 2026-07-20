package io.bookwright.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

public class RoomRowMapper implements RowMapper<RoomRow> {

    @Override
    public RoomRow map(ResultSet rs, StatementContext ctx) throws SQLException {
        return RoomRow.builder()
                .id(rs.getInt("id"))
                .name(rs.getString("name"))
                .type(rs.getString("type"))
                .price(rs.getBigDecimal("price"))
                .build();
    }
}
