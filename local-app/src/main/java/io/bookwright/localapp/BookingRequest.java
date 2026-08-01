package io.bookwright.localapp;

import java.time.LocalDate;

public record BookingRequest(
    Integer roomId,
    String guestFirstName,
    String guestLastName,
    LocalDate checkin,
    LocalDate checkout,
    Boolean depositPaid) {}
