package io.bookwright.localapp;

import java.time.LocalDate;

public record BookingResponse(
    int id,
    int roomId,
    String guestFirstName,
    String guestLastName,
    LocalDate checkin,
    LocalDate checkout,
    boolean depositPaid) {}
