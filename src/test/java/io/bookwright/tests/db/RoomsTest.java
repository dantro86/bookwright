package io.bookwright.tests.db;

import static org.assertj.core.api.Assertions.assertThat;

import io.bookwright.annotations.Db;
import io.bookwright.annotations.OwnerDanil;
import io.bookwright.annotations.Regression;
import io.bookwright.fixtures.database.HotelDatabaseFixtures;
import io.bookwright.steps.DbSteps;
import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@Db
@Regression
@OwnerDanil
@Feature("Hotel DB")
class RoomsTest {

  @Test
  @DisplayName("Seeded schema contains all rooms")
  void seededRoomsArePresent(DbSteps db, HotelDatabaseFixtures fixtures) {
    db.assertRoomCount(fixtures.roomCount());
    db.assertRoomsOfTypeExist(fixtures.roomType());
  }

  @Test
  @DisplayName("Join query finds rooms booked by a guest")
  void roomsBookedByGuestAreFound(DbSteps db, HotelDatabaseFixtures fixtures) {
    assertThat(db.roomsBookedByGuest(fixtures.seededGuestLastName()))
        .as("rooms booked by seeded guest")
        .isNotEmpty();
  }
}
