package io.bookwright.tests.integration;

import static org.assertj.core.api.Assertions.assertThat;

import io.bookwright.annotations.Api;
import io.bookwright.annotations.Db;
import io.bookwright.annotations.OwnerDanil;
import io.bookwright.annotations.Regression;
import io.bookwright.api.model.LocalBooking;
import io.bookwright.steps.ApiSteps;
import io.bookwright.steps.DbSteps;
import io.bookwright.util.TestData;
import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@Api
@Db
@Regression
@OwnerDanil
@Feature("Integrated local booking system")
class ApiDatabaseBookingTest {

  @Test
  @DisplayName("API booking is persisted to MySQL and removed through the API")
  void apiBookingIsPersistedAndCleanedUp(ApiSteps api, DbSteps db, TestData data) {
    LocalBooking created = api.local().bookings().create(data.localBooking());

    assertThat(api.local().bookings().get(created.getId()))
        .as("booking returned by the local API")
        .usingRecursiveComparison()
        .isEqualTo(created);
    db.assertBookingMatches(created);

    api.local().bookings().delete(created.getId());
    db.assertBookingAbsent(created.getId());
  }
}
