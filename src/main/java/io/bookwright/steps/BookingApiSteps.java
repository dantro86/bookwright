package io.bookwright.steps;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.inject.Inject;
import io.bookwright.api.AuthSession;
import io.bookwright.api.BookingApi;
import io.bookwright.api.model.Booking;
import io.bookwright.api.model.BookingId;
import io.bookwright.api.model.CreatedBooking;
import io.bookwright.teardown.TeardownStorage;
import io.bookwright.util.Calls;
import io.bookwright.util.Waits;
import io.qameta.allure.Step;
import java.util.List;

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
    CreatedBooking created = Calls.body(bookingApi.createBooking(booking), 200, "created booking");
    assertThat(created.getBookingid()).as("created booking id").isNotNull();
    teardown.push(
        "delete booking " + created.getBookingid(), () -> deleteQuietly(created.getBookingid()));
    return created;
  }

  @Step("Get booking {id}")
  public Booking get(int id) {
    return Calls.body(bookingApi.getBooking(id), 200, "booking " + id);
  }

  @Step("Get all booking ids")
  public List<BookingId> getIds() {
    return Calls.body(bookingApi.getBookingIds(), 200, "booking id list");
  }

  @Step("Update booking {id}")
  public Booking update(int id, Booking booking, AuthSession session) {
    return Calls.body(
        bookingApi.updateBooking(id, booking, session.cookie()), 200, "updated booking " + id);
  }

  @Step("Partially update booking {id}")
  public Booking partialUpdate(int id, Booking partial, AuthSession session) {
    return Calls.body(
        bookingApi.partialUpdateBooking(id, partial, session.cookie()),
        200,
        "partially updated booking " + id);
  }

  @Step("Find booking ids by guest name {firstname} {lastname}")
  public List<BookingId> findIdsByName(String firstname, String lastname) {
    return Calls.body(
        bookingApi.findBookingIds(firstname, lastname), 200, "booking search results");
  }

  /**
   * Eventual-consistency wait example: polls the search endpoint until the created booking shows
   * up. Note the raw API call inside the lambda — calling a @Step method in a polling loop would
   * flood the Allure report with a step per poll attempt.
   */
  @Step("Wait until booking {id} is searchable by guest name {firstname} {lastname}")
  public void waitUntilSearchableByName(int id, String firstname, String lastname) {
    Waits.await("booking %d in search results by name %s %s".formatted(id, firstname, lastname))
        .until(
            () ->
                Calls.body(
                        bookingApi.findBookingIds(firstname, lastname),
                        200,
                        "booking search results")
                    .stream()
                    .anyMatch(found -> found.getBookingid() == id));
  }

  @Step("Check update without auth token is forbidden for booking {id}")
  public void assertUpdateWithoutTokenForbidden(int id, Booking booking) {
    Calls.expectStatus(bookingApi.updateBooking(id, booking, ""), 403);
  }

  @Step("Check booking {id} does not exist")
  public void assertBookingNotFound(int id) {
    Calls.expectStatus(bookingApi.getBooking(id), 404);
  }

  @Step("Delete booking {id}")
  public void delete(int id, AuthSession session) {
    Calls.expectStatus(bookingApi.deleteBooking(id, session.cookie()), 201);
  }

  @Step("Check booking {id} matches expected data")
  public void assertBookingMatches(int id, Booking expected) {
    Booking actual = get(id);
    assertThat(actual).as("booking %d", id).isEqualTo(expected);
  }

  private void deleteQuietly(int id) {
    var response = Calls.response(bookingApi.deleteBooking(id, auth.session().cookie()));
    Calls.expectStatus(response, 201, 404, 405);
  }
}
