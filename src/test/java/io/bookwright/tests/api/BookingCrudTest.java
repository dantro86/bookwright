package io.bookwright.tests.api;

import io.bookwright.annotations.Api;
import io.bookwright.annotations.OwnerDanil;
import io.bookwright.annotations.Regression;
import io.bookwright.api.model.Booking;
import io.bookwright.api.model.CreatedBooking;
import io.bookwright.junit.NamespaceRegistry;
import io.bookwright.junit.Preconditions;
import io.bookwright.junit.TestStore;
import io.bookwright.junit.WithAuthSession;
import io.bookwright.steps.ApiSteps;
import io.bookwright.util.BookingFactory;
import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.bookwright.junit.Precondition.BOOKING_EXISTS;
import static org.assertj.core.api.Assertions.assertThat;

@Api
@Regression
@OwnerDanil
@Feature("Bookings")
class BookingCrudTest {

    @Test
    @DisplayName("Booking can be created and read back")
    void bookingCanBeCreated(ApiSteps api) {
        Booking booking = BookingFactory.random();
        CreatedBooking created = api.bookings().create(booking);
        api.bookings().assertBookingMatches(created.getBookingid(), booking);
    }

    @Test
    @WithAuthSession
    @Preconditions({BOOKING_EXISTS})
    @DisplayName("Booking can be updated")
    void bookingCanBeUpdated(ApiSteps api, TestStore store) {
        CreatedBooking existing = store.get(NamespaceRegistry.BOOKING_KEY, CreatedBooking.class);
        Booking updated = BookingFactory.random();
        api.bookings().update(existing.getBookingid(), updated);
        api.bookings().assertBookingMatches(existing.getBookingid(), updated);
    }

    @Test
    @WithAuthSession
    @Preconditions({BOOKING_EXISTS})
    @DisplayName("Booking can be deleted")
    void bookingCanBeDeleted(ApiSteps api, TestStore store) {
        CreatedBooking existing = store.get(NamespaceRegistry.BOOKING_KEY, CreatedBooking.class);
        api.bookings().delete(existing.getBookingid());
        assertThat(api.bookings().getIds())
                .as("booking ids after deletion")
                .noneMatch(id -> id.getBookingid().equals(existing.getBookingid()));
    }
}
