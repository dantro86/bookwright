package io.bookwright.db;

import java.util.List;
import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.SqlQuery;

public interface RoomDao {

    @SqlQuery("SELECT COUNT(*) FROM room")
    int countRooms();

    @RegisterRowMapper(RoomRowMapper.class)
    @SqlQuery("SELECT * FROM room WHERE type = :type")
    List<RoomRow> findByType(@Bind("type") String type);

    @RegisterRowMapper(RoomRowMapper.class)
    @SqlQuery("""
            SELECT r.* FROM room r
            JOIN booking b ON b.room_id = r.id
            WHERE b.guest_last_name = :lastName
            """)
    List<RoomRow> findRoomsBookedByGuest(@Bind("lastName") String lastName);
}
