package io.bookwright.steps.ui.saucedemo.login;

import com.google.inject.Inject;
import com.microsoft.playwright.assertions.PlaywrightAssertions;
import io.bookwright.config.MainConfig;
import io.bookwright.ui.LoginPage;
import io.qameta.allure.Step;

public class LoginSteps {

  private final LoginPage page;
  private final MainConfig config;

  @Inject
  public LoginSteps(LoginPage page, MainConfig config) {
    this.page = page;
    this.config = config;
  }

  @Step("Log in to Sauce Demo as the configured standard user")
  public void asStandardUser() {
    page.open();
    page.login(config.uiUser(), config.uiPassword());
  }

  @Step("Log in to Sauce Demo with an invalid password")
  public void withInvalidPasswordAndExpectError() {
    page.open();
    page.login(config.uiUser(), "definitely-wrong");
    PlaywrightAssertions.assertThat(page.errorMessage())
        .containsText("Username and password do not match");
  }

  @Step("Log in to Sauce Demo as a locked-out user")
  public void asLockedOutUserAndExpectError() {
    page.open();
    page.login("locked_out_user", config.uiPassword());
    PlaywrightAssertions.assertThat(page.errorMessage())
        .containsText("Sorry, this user has been locked out");
  }
}
