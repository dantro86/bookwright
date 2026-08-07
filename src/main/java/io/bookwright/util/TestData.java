package io.bookwright.util;

import io.bookwright.api.model.Booking;
import io.bookwright.api.model.LocalBooking;
import io.bookwright.api.model.UserRegistration;
import io.bookwright.db.BookingRow;
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

  public Booking bookingPatch() {
    return Booking.builder().firstname("Patched-" + Long.toUnsignedString(testSeed, 36)).build();
  }

  public int nonexistentBookingId() {
    return 900_000_000 + (int) Math.floorMod(testSeed, 99_999_999);
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

  public BookingRow databaseBooking() {
    LocalBooking booking = localBooking();
    return BookingRow.builder()
        .roomId(booking.getRoomId())
        .guestFirstName(booking.getGuestFirstName())
        .guestLastName(booking.getGuestLastName())
        .checkin(booking.getCheckin())
        .checkout(booking.getCheckout())
        .depositPaid(booking.getDepositPaid())
        .build();
  }

  public UserRegistration user() {
    String suffix = Long.toUnsignedString(testSeed, 36);
    return new UserRegistration(
        "new.%s@bookwright.dev".formatted(suffix),
        "Bw!%sAa9".formatted(suffix),
        "New User %s".formatted(suffix.substring(0, Math.min(8, suffix.length()))));
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
