package io.bookwright.db;

import java.time.LocalDate;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class BookingRow {
    Integer id;
    Integer roomId;
    String guestFirstName;
    String guestLastName;
    LocalDate checkin;
    LocalDate checkout;
    Boolean depositPaid;
}
