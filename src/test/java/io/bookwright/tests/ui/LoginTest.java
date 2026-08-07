package io.bookwright.tests.ui;

import io.bookwright.annotations.OwnerDanil;
import io.bookwright.annotations.Smoke;
import io.bookwright.annotations.Ui;
import io.bookwright.fixtures.saucedemo.SauceDemoFixtures;
import io.bookwright.steps.UiSteps;
import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@Ui
@Smoke
@OwnerDanil
@Feature("Login")
class LoginTest {

  @Test
  @DisplayName("Standard user can log in")
  void standardUserCanLogIn(UiSteps ui, SauceDemoFixtures fixtures) {
    ui.sauceDemo().login().login(fixtures.standardUser());
    ui.sauceDemo().inventory().assertReady(fixtures.catalog());
  }

  @Test
  @DisplayName("Invalid password shows an error")
  void invalidPasswordShowsError(UiSteps ui, SauceDemoFixtures fixtures) {
    ui.sauceDemo().login().loginAndExpectError(fixtures.invalidPassword());
  }
}
