package io.bookwright.steps;

import com.google.inject.Inject;
import io.bookwright.db.BookingDao;
import io.bookwright.db.BookingRow;
import io.bookwright.db.RoomDao;
import io.bookwright.db.RoomRow;
import io.bookwright.teardown.TeardownStorage;
import io.qameta.allure.Step;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class DbSteps {

    private final BookingDao bookingDao;
    private final RoomDao roomDao;
    private final TeardownStorage teardown;

    @Inject
    public DbSteps(BookingDao bookingDao, RoomDao roomDao, TeardownStorage teardown) {
        this.bookingDao = bookingDao;
        this.roomDao = roomDao;
        this.teardown = teardown;
    }

    @Step("Check there are at least {expectedCount} bookings in the DB")
    public void assertBookingCountAtLeast(int expectedCount) {
        assertThat(bookingDao.countBookings())
                .as("bookings in DB")
                .isGreaterThanOrEqualTo(expectedCount);
    }

    @Step("Check guest '{lastName}' has at least one booking")
    public void assertGuestHasBooking(String lastName) {
        assertThat(bookingDao.findByGuestLastName(lastName))
                .as("bookings of guest %s", lastName)
                .isNotEmpty();
    }

    @Step("Insert booking for {booking.guestFirstName} {booking.guestLastName}")
    public BookingRow insert(BookingRow booking) {
        int id = bookingDao.insert(booking);
        teardown.push("delete DB booking " + id, () -> bookingDao.deleteById(id));
        BookingRow saved = bookingDao.findById(id);
        assertThat(saved).as("booking %d after insert", id).isNotNull();
        return saved;
    }

    @Step("Check booking {id} is absent")
    public void assertBookingAbsent(int id) {
        assertThat(bookingDao.findById(id)).as("booking %d", id).isNull();
    }

    @Step("Check there are exactly {expectedCount} rooms")
    public void assertRoomCount(int expectedCount) {
        assertThat(roomDao.countRooms()).as("rooms in DB").isEqualTo(expectedCount);
    }

    @Step("Check rooms of type '{type}' exist")
    public List<RoomRow> assertRoomsOfTypeExist(String type) {
        List<RoomRow> rooms = roomDao.findByType(type);
        assertThat(rooms).as("rooms of type %s", type).isNotEmpty();
        return rooms;
    }

    @Step("Check guest '{lastName}' has booked rooms")
    public List<RoomRow> roomsBookedByGuest(String lastName) {
        return roomDao.findRoomsBookedByGuest(lastName);
    }
}
