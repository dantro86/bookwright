package io.bookwright.db;

import java.util.List;
import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

public interface BookingDao {

    @SqlQuery("SELECT COUNT(*) FROM booking")
    int countBookings();

    @RegisterRowMapper(BookingRowMapper.class)
    @SqlQuery("SELECT * FROM booking WHERE guest_last_name = :lastName")
    List<BookingRow> findByGuestLastName(@Bind("lastName") String lastName);

    @RegisterRowMapper(BookingRowMapper.class)
    @SqlQuery("SELECT * FROM booking WHERE id = :id")
    BookingRow findById(@Bind("id") int id);

    @SqlUpdate("""
            INSERT INTO booking (room_id, guest_first_name, guest_last_name, checkin, checkout, deposit_paid)
            VALUES (:roomId, :guestFirstName, :guestLastName, :checkin, :checkout, :depositPaid)
            """)
    @GetGeneratedKeys
    int insert(@BindBean BookingRow booking);

    @SqlUpdate("DELETE FROM booking WHERE id = :id")
    void deleteById(@Bind("id") int id);
}
