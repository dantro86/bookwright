package io.bookwright.fixtures.database;

import com.google.inject.Inject;

/** Expectations owned by the deterministic MySQL seed script. */
public record HotelDatabaseFixtures(
    int minimumBookingCount, String seededGuestLastName, int roomCount, String roomType) {

  @Inject
  public HotelDatabaseFixtures() {
    this(10, "Wilson", 5, "double");
  }

  public static HotelDatabaseFixtures seeded() {
    return new HotelDatabaseFixtures();
  }
}
