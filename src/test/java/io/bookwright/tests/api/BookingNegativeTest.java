package io.bookwright.tests.api;

import io.bookwright.annotations.Api;
import io.bookwright.annotations.OwnerDanil;
import io.bookwright.annotations.Regression;
import io.bookwright.api.model.CreatedBooking;
import io.bookwright.junit.NamespaceRegistry;
import io.bookwright.junit.Preconditions;
import io.bookwright.junit.TestStore;
import io.bookwright.steps.ApiSteps;
import io.bookwright.util.BookingFactory;
import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.bookwright.junit.Precondition.BOOKING_EXISTS;

@Api
@Regression
@OwnerDanil
@Feature("Bookings")
class BookingNegativeTest {

    private static final int NONEXISTENT_ID = 999_999_999;

    @Test
    @Preconditions({BOOKING_EXISTS})
    @DisplayName("Update without auth token is forbidden")
    void updateWithoutTokenIsForbidden(ApiSteps api, TestStore store) {
        CreatedBooking existing = store.get(NamespaceRegistry.BOOKING_KEY, CreatedBooking.class);
        api.bookings().assertUpdateWithoutTokenForbidden(existing.getBookingid(), BookingFactory.random());
    }

    @Test
    @DisplayName("Requesting a nonexistent booking returns 404")
    void nonexistentBookingReturns404(ApiSteps api) {
        api.bookings().assertBookingNotFound(NONEXISTENT_ID);
    }
}
