package io.bookwright.steps.ui.saucedemo.login;

import com.google.inject.Inject;
import com.microsoft.playwright.assertions.PlaywrightAssertions;
import io.bookwright.fixtures.saucedemo.SauceDemoFixtures.LoginCase;
import io.bookwright.fixtures.saucedemo.SauceDemoFixtures.User;
import io.bookwright.ui.LoginPage;
import io.qameta.allure.Step;

public class LoginSteps {

  private final LoginPage page;

  @Inject
  public LoginSteps(LoginPage page) {
    this.page = page;
  }

  @Step("Log in to Sauce Demo as {user.username}")
  public void login(User user) {
    page.open();
    page.login(user.username(), user.password());
  }

  @Step("Attempt Sauce Demo login as {scenario.user.username} and verify rejection")
  public void loginAndExpectError(LoginCase scenario) {
    page.open();
    page.login(scenario.user().username(), scenario.user().password());
    PlaywrightAssertions.assertThat(page.errorMessage()).containsText(scenario.expectedError());
  }
}
