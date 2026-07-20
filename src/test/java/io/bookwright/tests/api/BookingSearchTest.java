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
import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.bookwright.junit.Precondition.BOOKING_EXISTS;
import static org.assertj.core.api.Assertions.assertThat;

@Api
@Regression
@OwnerDanil
@Feature("Bookings")
class BookingSearchTest {

    @Test
    @Preconditions({BOOKING_EXISTS})
    @DisplayName("Booking can be found by guest name")
    void bookingCanBeFoundByName(ApiSteps api, TestStore store) {
        CreatedBooking existing = store.get(NamespaceRegistry.BOOKING_KEY, CreatedBooking.class);
        Booking booking = existing.getBooking();
        assertThat(api.bookings().findIdsByName(booking.getFirstname(), booking.getLastname()))
                .as("bookings found by guest name")
                .anyMatch(id -> id.getBookingid().equals(existing.getBookingid()));
    }

    @Test
    @Preconditions({BOOKING_EXISTS})
    @DisplayName("Created booking becomes searchable (Awaitility polling)")
    void createdBookingBecomesSearchable(ApiSteps api, TestStore store) {
        CreatedBooking existing = store.get(NamespaceRegistry.BOOKING_KEY, CreatedBooking.class);
        Booking booking = existing.getBooking();
        api.bookings().waitUntilSearchableByName(
                existing.getBookingid(), booking.getFirstname(), booking.getLastname());
    }

    @Test
    @WithAuthSession
    @Preconditions({BOOKING_EXISTS})
    @DisplayName("PATCH updates only the provided fields")
    void partialUpdateChangesOnlyProvidedFields(ApiSteps api, TestStore store) {
        CreatedBooking existing = store.get(NamespaceRegistry.BOOKING_KEY, CreatedBooking.class);
        Booking original = existing.getBooking();

        api.bookings().partialUpdate(existing.getBookingid(),
                Booking.builder().firstname("Patched").build());

        Booking after = api.bookings().get(existing.getBookingid());
        assertThat(after.getFirstname()).as("patched firstname").isEqualTo("Patched");
        assertThat(after.getLastname()).as("untouched lastname").isEqualTo(original.getLastname());
        assertThat(after.getTotalprice()).as("untouched totalprice").isEqualTo(original.getTotalprice());
    }
}
