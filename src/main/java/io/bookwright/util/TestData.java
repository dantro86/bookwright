package io.bookwright.util;

import io.bookwright.api.model.Booking;
import io.bookwright.api.model.LocalBooking;
import java.util.SplittableRandom;

/**
 * Per-test deterministic data source. Its sequence depends only on the run seed and test identity,
 * never on execution order or parallel scheduling.
 */
public final class TestData {

  private final long runSeed;
  private final long testSeed;
  private final String testId;
  private final BookingFactory bookings;

  public TestData(long runSeed, long testSeed, String testId) {
    this.runSeed = runSeed;
    this.testSeed = testSeed;
    this.testId = testId;
    this.bookings = new BookingFactory(new SplittableRandom(testSeed));
  }

  public Booking booking() {
    return bookings.next();
  }

  public LocalBooking localBooking() {
    Booking booking = booking();
    int roomId = (int) Math.floorMod(testSeed, 5) + 1;
    return LocalBooking.builder()
        .roomId(roomId)
        .guestFirstName(booking.getFirstname())
        .guestLastName(booking.getLastname())
        .checkin(booking.getBookingdates().getCheckin())
        .checkout(booking.getBookingdates().getCheckout())
        .depositPaid(booking.getDepositpaid())
        .build();
  }

  public long runSeed() {
    return runSeed;
  }

  public long testSeed() {
    return testSeed;
  }

  public String testId() {
    return testId;
  }
}
