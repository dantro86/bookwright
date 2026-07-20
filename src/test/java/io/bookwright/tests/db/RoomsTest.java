package io.bookwright.tests.db;

import io.bookwright.annotations.Db;
import io.bookwright.annotations.OwnerDanil;
import io.bookwright.annotations.Regression;
import io.bookwright.steps.DbSteps;
import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Db
@Regression
@OwnerDanil
@Feature("Hotel DB")
class RoomsTest {

    @Test
    @DisplayName("Seeded schema contains all rooms")
    void seededRoomsArePresent(DbSteps db) {
        db.assertRoomCount(5);
        db.assertRoomsOfTypeExist("double");
    }

    @Test
    @DisplayName("Join query finds rooms booked by a guest")
    void roomsBookedByGuestAreFound(DbSteps db) {
        assertThat(db.roomsBookedByGuest("Wilson"))
                .as("rooms booked by Wilson")
                .isNotEmpty();
    }
}
