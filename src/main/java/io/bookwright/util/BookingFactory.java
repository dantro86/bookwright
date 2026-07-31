package io.bookwright.util;

import io.bookwright.api.model.Booking;
import io.bookwright.api.model.BookingDates;
import java.time.LocalDate;
import java.util.SplittableRandom;

/** Produces a deterministic sequence of valid booking payloads from an explicit random source. */
public final class BookingFactory {

  private static final LocalDate DATE_ANCHOR = LocalDate.of(2040, 1, 1);
  private final SplittableRandom random;

  public BookingFactory(SplittableRandom random) {
    this.random = random;
  }

  public Booking next() {
    LocalDate checkin = DATE_ANCHOR.plusDays(random.nextInt(1, 365));
    return Booking.builder()
        .firstname("Test")
        .lastname("Guest-" + randomSuffix())
        .totalprice(random.nextInt(50, 500))
        .depositpaid(true)
        .bookingdates(
            BookingDates.builder()
                .checkin(checkin)
                .checkout(checkin.plusDays(random.nextInt(1, 14)))
                .build())
        .additionalneeds("Breakfast")
        .build();
  }

  private String randomSuffix() {
    String value = Long.toUnsignedString(random.nextLong(), 36);
    if (value.length() >= 8) {
      return value.substring(0, 8);
    }
    return "0".repeat(8 - value.length()) + value;
  }
}
