package io.bookwright.util;

import io.bookwright.api.model.Booking;
import io.bookwright.api.model.BookingDates;
import java.time.LocalDate;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import lombok.experimental.UtilityClass;

/**
 * Produces valid randomized booking payloads so tests never collide on data.
 */
@UtilityClass
public class BookingFactory {

    public Booking random() {
        LocalDate checkin = LocalDate.now().plusDays(ThreadLocalRandom.current().nextInt(1, 30));
        return Booking.builder()
                .firstname("Test")
                .lastname("Guest-" + UUID.randomUUID().toString().substring(0, 8))
                .totalprice(ThreadLocalRandom.current().nextInt(50, 500))
                .depositpaid(true)
                .bookingdates(BookingDates.builder()
                        .checkin(checkin)
                        .checkout(checkin.plusDays(ThreadLocalRandom.current().nextInt(1, 14)))
                        .build())
                .additionalneeds("Breakfast")
                .build();
    }
}
