package io.bookwright.tests.db;

import io.bookwright.annotations.Db;
import io.bookwright.annotations.OwnerDanil;
import io.bookwright.annotations.Smoke;
import io.bookwright.db.BookingRow;
import io.bookwright.steps.DbSteps;
import io.qameta.allure.Feature;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Db
@Smoke
@OwnerDanil
@Feature("Hotel DB")
class SeededBookingsTest {

    private static final LocalDate FIXED_CHECKIN = LocalDate.of(2040, 6, 15);

    @Test
    @DisplayName("Seeded schema contains the expected bookings")
    void seededBookingsArePresent(DbSteps db) {
        db.assertBookingCountAtLeast(10);
        db.assertGuestHasBooking("Wilson");
    }

    @Test
    @DisplayName("Booking can be inserted and read back through the tunnel")
    void bookingCanBeInserted(DbSteps db) {
        BookingRow inserted = db.insert(BookingRow.builder()
                .roomId(1)
                .guestFirstName("Tunnel")
                .guestLastName("Tester")
                .checkin(FIXED_CHECKIN)
                .checkout(FIXED_CHECKIN.plusDays(2))
                .depositPaid(true)
                .build());
        assertThat(inserted.getGuestLastName()).isEqualTo("Tester");
    } // row removed by LIFO teardown
}
