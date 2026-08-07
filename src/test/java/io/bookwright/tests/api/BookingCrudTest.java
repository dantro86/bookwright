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
class BookingCrudTest {

  @Test
  @WithAuthSession
  @DisplayName("Booking can be created and read back")
  void bookingCanBeCreated(ApiSteps api, TestStore store, TestData data) {
    Booking booking = data.booking();
    CreatedBooking created = api.restfulBooker().bookings().create(booking, store.authSession());
    api.restfulBooker().bookings().assertBookingMatches(created.getBookingid(), booking);
  }

  @Test
  @WithAuthSession
  @Preconditions({BOOKING_EXISTS})
  @DisplayName("Booking can be updated")
  void bookingCanBeUpdated(ApiSteps api, TestStore store, TestData data) {
    CreatedBooking existing = store.booking();
    Booking updated = data.booking();
    api.restfulBooker().bookings().update(existing.getBookingid(), updated, store.authSession());
    api.restfulBooker().bookings().assertBookingMatches(existing.getBookingid(), updated);
  }

  @Test
  @WithAuthSession
  @Preconditions({BOOKING_EXISTS})
  @DisplayName("Booking can be deleted")
  void bookingCanBeDeleted(ApiSteps api, TestStore store) {
    CreatedBooking existing = store.booking();
    api.restfulBooker().bookings().delete(existing.getBookingid(), store.authSession());
    assertThat(api.restfulBooker().bookings().getIds())
        .as("booking ids after deletion")
        .noneMatch(id -> id.getBookingid().equals(existing.getBookingid()));
  }
}
