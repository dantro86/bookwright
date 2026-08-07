package io.bookwright.tests.api;

import static io.bookwright.junit.Precondition.BOOKING_EXISTS;
import static org.assertj.core.api.Assertions.assertThat;

import io.bookwright.annotations.Api;
import io.bookwright.annotations.OwnerDanil;
import io.bookwright.annotations.Regression;
import io.bookwright.api.model.Booking;
import io.bookwright.api.model.CreatedBooking;
import io.bookwright.junit.Preconditions;
import io.bookwright.junit.TestStore;
import io.bookwright.junit.WithAuthSession;
import io.bookwright.steps.ApiSteps;
import io.bookwright.util.TestData;
import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@Api
@Regression
@OwnerDanil
@Feature("Bookings")
class BookingSearchTest {

  @Test
  @Preconditions({BOOKING_EXISTS})
  @DisplayName("Booking can be found by guest name")
  void bookingCanBeFoundByName(ApiSteps api, TestStore store) {
    CreatedBooking existing = store.booking();
    Booking booking = existing.getBooking();
    assertThat(
            api.restfulBooker()
                .bookings()
                .findIdsByName(booking.getFirstname(), booking.getLastname()))
        .as("bookings found by guest name")
        .anyMatch(id -> id.getBookingid().equals(existing.getBookingid()));
  }

  @Test
  @Preconditions({BOOKING_EXISTS})
  @DisplayName("Created booking becomes searchable (Awaitility polling)")
  void createdBookingBecomesSearchable(ApiSteps api, TestStore store) {
    CreatedBooking existing = store.booking();
    Booking booking = existing.getBooking();
    api.restfulBooker()
        .bookings()
        .waitUntilSearchableByName(
            existing.getBookingid(), booking.getFirstname(), booking.getLastname());
  }

  @Test
  @WithAuthSession
  @Preconditions({BOOKING_EXISTS})
  @DisplayName("PATCH updates only the provided fields")
  void partialUpdateChangesOnlyProvidedFields(ApiSteps api, TestStore store, TestData data) {
    CreatedBooking existing = store.booking();
    Booking original = existing.getBooking();

    Booking patch = data.bookingPatch();
    api.restfulBooker()
        .bookings()
        .partialUpdate(existing.getBookingid(), patch, store.authSession());

    Booking after = api.restfulBooker().bookings().get(existing.getBookingid());
    assertThat(after.getFirstname()).as("patched firstname").isEqualTo(patch.getFirstname());
    assertThat(after.getLastname()).as("untouched lastname").isEqualTo(original.getLastname());
    assertThat(after.getTotalprice())
        .as("untouched totalprice")
        .isEqualTo(original.getTotalprice());
  }
}
