package io.bookwright.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

/** Null fields are omitted from JSON so the same model works for PATCH partial updates. */
@Value
@Builder
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Booking {
    String firstname;
    String lastname;
    Integer totalprice;
    Boolean depositpaid;
    BookingDates bookingdates;
    String additionalneeds;
}
