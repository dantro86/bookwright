package io.bookwright.steps;

import com.google.inject.Inject;
import io.bookwright.api.BookingApi;
import io.bookwright.api.model.Booking;
import io.bookwright.api.model.BookingId;
import io.bookwright.api.model.CreatedBooking;
import io.bookwright.teardown.TeardownStorage;
import io.bookwright.util.Calls;
import io.qameta.allure.Step;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class BookingApiSteps {

    private final BookingApi bookingApi;
    private final AuthApiSteps auth;
    private final TeardownStorage teardown;

    @Inject
    public BookingApiSteps(BookingApi bookingApi, AuthApiSteps auth, TeardownStorage teardown) {
        this.bookingApi = bookingApi;
        this.auth = auth;
        this.teardown = teardown;
    }

    @Step("Create booking for {booking.firstname} {booking.lastname}")
    public CreatedBooking create(Booking booking) {
        CreatedBooking created = Calls.unwrap(bookingApi.createBooking(booking), 200);
        assertThat(created.getBookingid()).as("created booking id").isNotNull();
        teardown.push("delete booking " + created.getBookingid(),
                () -> deleteQuietly(created.getBookingid()));
        return created;
    }

    @Step("Get booking {id}")
    public Booking get(int id) {
        return Calls.unwrap(bookingApi.getBooking(id), 200);
    }

    @Step("Get all booking ids")
    public List<BookingId> getIds() {
        return Calls.unwrap(bookingApi.getBookingIds(), 200);
    }

    @Step("Update booking {id}")
    public Booking update(int id, Booking booking) {
        return Calls.unwrap(bookingApi.updateBooking(id, booking, tokenCookie()), 200);
    }

    @Step("Partially update booking {id}")
    public Booking partialUpdate(int id, Booking partial) {
        return Calls.unwrap(bookingApi.partialUpdateBooking(id, partial, tokenCookie()), 200);
    }

    @Step("Find booking ids by guest name {firstname} {lastname}")
    public List<BookingId> findIdsByName(String firstname, String lastname) {
        return Calls.unwrap(bookingApi.findBookingIds(firstname, lastname), 200);
    }

    @Step("Check update without auth token is forbidden for booking {id}")
    public void assertUpdateWithoutTokenForbidden(int id, Booking booking) {
        Calls.checkStatus(Calls.execute(bookingApi.updateBooking(id, booking, "")), 403);
    }

    @Step("Check booking {id} does not exist")
    public void assertBookingNotFound(int id) {
        Calls.checkStatus(Calls.execute(bookingApi.getBooking(id)), 404);
    }

    @Step("Delete booking {id}")
    public void delete(int id) {
        Calls.checkStatus(Calls.execute(bookingApi.deleteBooking(id, tokenCookie())), 201);
    }

    @Step("Check booking {id} matches expected data")
    public void assertBookingMatches(int id, Booking expected) {
        Booking actual = get(id);
        assertThat(actual).as("booking %d", id).isEqualTo(expected);
    }

    private void deleteQuietly(int id) {
        Calls.execute(bookingApi.deleteBooking(id, tokenCookie()));
    }

    private String tokenCookie() {
        return "token=" + auth.token();
    }
}
