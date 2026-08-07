package io.bookwright.fixtures.database;

/** Expectations owned by the deterministic MySQL seed script. */
public record HotelDatabaseFixtures(
    int minimumBookingCount, String seededGuestLastName, int roomCount, String roomType) {

  public static HotelDatabaseFixtures seeded() {
    return new HotelDatabaseFixtures(10, "Wilson", 5, "double");
  }
}
