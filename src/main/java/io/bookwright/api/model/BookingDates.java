package io.bookwright.api.model;

import java.time.LocalDate;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class BookingDates {
    LocalDate checkin;
    LocalDate checkout;
}
