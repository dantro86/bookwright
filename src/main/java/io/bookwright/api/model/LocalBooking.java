package io.bookwright.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LocalBooking {
  Integer id;
  Integer roomId;
  String guestFirstName;
  String guestLastName;
  LocalDate checkin;
  LocalDate checkout;
  Boolean depositPaid;
}
