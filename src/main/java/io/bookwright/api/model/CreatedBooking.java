package io.bookwright.api.model;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class CreatedBooking {
    Integer bookingid;
    Booking booking;
}
