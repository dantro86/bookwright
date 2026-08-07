package io.bookwright.steps.restfulbooker.bookings;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.inject.Inject;
import io.bookwright.api.AuthSession;
import io.bookwright.api.model.Booking;
import io.bookwright.api.model.BookingId;
import io.bookwright.api.model.CreatedBooking;
import io.bookwright.api.restfulbooker.bookings.BookingsApi;
import io.bookwright.teardown.TeardownStorage;
import io.bookwright.util.Calls;
import io.bookwright.util.Waits;
import io.qameta.allure.Step;
import java.util.List;

public class BookingSteps {

  private final BookingsApi api;
  private final TeardownStorage teardown;

  @Inject
  public BookingSteps(BookingsApi api, TeardownStorage teardown) {
    this.api = api;
    this.teardown = teardown;
  }

  @Step("Create restful-booker booking for {booking.firstname} {booking.lastname}")
  public CreatedBooking create(Booking booking, AuthSession session) {
    CreatedBooking created = Calls.body(api.createBooking(booking), 200, "created booking");
    assertThat(created.getBookingid()).as("created booking id").isNotNull();
    teardown.push(
        "delete booking " + created.getBookingid(),
        () -> deleteQuietly(created.getBookingid(), session));
    return created;
  }

  @Step("Get restful-booker booking {id}")
  public Booking get(int id) {
    return Calls.body(api.getBooking(id), 200, "booking " + id);
  }

  @Step("Get all restful-booker booking ids")
  public List<BookingId> getIds() {
    return Calls.body(api.getBookingIds(), 200, "booking id list");
  }

  @Step("Update restful-booker booking {id}")
  public Booking update(int id, Booking booking, AuthSession session) {
    return Calls.body(
        api.updateBooking(id, booking, session.cookie()), 200, "updated booking " + id);
  }

  @Step("Partially update restful-booker booking {id}")
  public Booking partialUpdate(int id, Booking partial, AuthSession session) {
    return Calls.body(
        api.partialUpdateBooking(id, partial, session.cookie()),
        200,
        "partially updated booking " + id);
  }

  @Step("Find restful-booker booking ids by guest name {firstname} {lastname}")
  public List<BookingId> findIdsByName(String firstname, String lastname) {
    return Calls.body(api.findBookingIds(firstname, lastname), 200, "booking search results");
  }

  @Step("Wait until booking {id} is searchable by guest name {firstname} {lastname}")
  public void waitUntilSearchableByName(int id, String firstname, String lastname) {
    Waits.await("booking %d in search results by name %s %s".formatted(id, firstname, lastname))
        .until(
            () ->
                Calls.body(api.findBookingIds(firstname, lastname), 200, "booking search results")
                    .stream()
                    .anyMatch(found -> found.getBookingid() == id));
  }

  @Step("Check update without auth token is forbidden for booking {id}")
  public void assertUpdateWithoutTokenForbidden(int id, Booking booking) {
    Calls.expectStatus(api.updateBooking(id, booking, ""), 403);
  }

  @Step("Check booking {id} does not exist")
  public void assertBookingNotFound(int id) {
    Calls.expectStatus(api.getBooking(id), 404);
  }

  @Step("Delete restful-booker booking {id}")
  public void delete(int id, AuthSession session) {
    Calls.expectStatus(api.deleteBooking(id, session.cookie()), 201);
  }

  @Step("Check restful-booker booking {id} matches expected data")
  public void assertBookingMatches(int id, Booking expected) {
    assertThat(get(id)).as("booking %d", id).isEqualTo(expected);
  }

  private void deleteQuietly(int id, AuthSession session) {
    var response = Calls.response(api.deleteBooking(id, session.cookie()));
    Calls.expectStatus(response, 201, 404, 405);
  }
}
