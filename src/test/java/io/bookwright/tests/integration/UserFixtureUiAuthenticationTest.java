package io.bookwright.tests.integration;

import static org.assertj.core.api.Assertions.assertThat;

import io.bookwright.annotations.Api;
import io.bookwright.annotations.OwnerDanil;
import io.bookwright.annotations.Regression;
import io.bookwright.annotations.Ui;
import io.bookwright.fixtures.local.LocalUserFixtures;
import io.bookwright.junit.TestUser;
import io.bookwright.junit.UserFixture;
import io.bookwright.junit.UserFixtureMode;
import io.bookwright.steps.ApiSteps;
import io.bookwright.steps.UiSteps;
import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

@Api
@Ui
@Regression
@OwnerDanil
@Feature("User fixtures and API-authenticated UI")
@Execution(ExecutionMode.CONCURRENT)
class UserFixtureUiAuthenticationTest {

  @Test
  @UserFixture(UserFixtureMode.NEW)
  @DisplayName("A newly registered user opens UI without submitting the login form")
  void newUserStartsWithAuthenticatedBrowserState(
      TestUser user, UiSteps ui, LocalUserFixtures fixtures) {
    assertThat(user.mode()).isEqualTo(UserFixtureMode.NEW);
    assertThat(user.profile().email()).isEqualTo(user.credentials().email());

    ui.local().bookings().openAs(user, fixtures.ui());
  }

  @Test
  @UserFixture(UserFixtureMode.EXISTING)
  @DisplayName("A configured existing user opens UI through a fresh API session")
  void existingUserStartsWithAuthenticatedBrowserState(
      TestUser user, UiSteps ui, LocalUserFixtures fixtures) {
    assertThat(user.mode()).isEqualTo(UserFixtureMode.EXISTING);

    ui.local().bookings().openAs(user, fixtures.ui());
  }

  @Test
  @DisplayName("UI rejects a browser context without an API session")
  void missingSessionIsRejected(UiSteps ui, LocalUserFixtures fixtures) {
    ui.local().bookings().openAndExpectAuthenticationRequired(fixtures.ui());
  }

  @Test
  @DisplayName("API rejects invalid user credentials")
  void invalidCredentialsAreRejected(ApiSteps api, LocalUserFixtures fixtures) {
    api.local().auth().expectLoginRejected(fixtures.invalidExistingUser());
  }
}
