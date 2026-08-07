package io.bookwright.tests.db;

import static org.assertj.core.api.Assertions.assertThat;

import io.bookwright.annotations.Db;
import io.bookwright.annotations.OwnerDanil;
import io.bookwright.annotations.Smoke;
import io.bookwright.db.BookingRow;
import io.bookwright.fixtures.database.HotelDatabaseFixtures;
import io.bookwright.steps.DbSteps;
import io.bookwright.util.TestData;
import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@Db
@Smoke
@OwnerDanil
@Feature("Hotel DB")
class SeededBookingsTest {

  @Test
  @DisplayName("Seeded schema contains the expected bookings")
  void seededBookingsArePresent(DbSteps db, HotelDatabaseFixtures fixtures) {
    db.assertBookingCountAtLeast(fixtures.minimumBookingCount());
    db.assertGuestHasBooking(fixtures.seededGuestLastName());
  }

  @Test
  @DisplayName("Booking can be inserted and read back through the tunnel")
  void bookingCanBeInserted(DbSteps db, TestData data) {
    BookingRow expected = data.databaseBooking();
    BookingRow inserted = db.insert(expected);
    assertThat(inserted.getGuestLastName()).isEqualTo(expected.getGuestLastName());
  } // row removed by LIFO teardown
}
