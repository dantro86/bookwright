package io.bookwright.tests.api;

import static io.bookwright.junit.Precondition.BOOKING_EXISTS;

import io.bookwright.annotations.Api;
import io.bookwright.annotations.OwnerDanil;
import io.bookwright.annotations.Regression;
import io.bookwright.api.model.CreatedBooking;
import io.bookwright.junit.Preconditions;
import io.bookwright.junit.TestStore;
import io.bookwright.steps.ApiSteps;
import io.bookwright.util.TestData;
import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@Api
@Regression
@OwnerDanil
@Feature("Bookings")
class BookingNegativeTest {

  @Test
  @Preconditions({BOOKING_EXISTS})
  @DisplayName("Update without auth token is forbidden")
  void updateWithoutTokenIsForbidden(ApiSteps api, TestStore store, TestData data) {
    CreatedBooking existing = store.booking();
    api.restfulBooker()
        .bookings()
        .assertUpdateWithoutTokenForbidden(existing.getBookingid(), data.booking());
  }

  @Test
  @DisplayName("Requesting a nonexistent booking returns 404")
  void nonexistentBookingReturns404(ApiSteps api, TestData data) {
    api.restfulBooker().bookings().assertBookingNotFound(data.nonexistentBookingId());
  }
}
