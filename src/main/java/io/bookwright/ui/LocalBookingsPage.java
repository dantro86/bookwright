package io.bookwright.ui;

import com.google.inject.Inject;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import io.bookwright.config.MainConfig;

public class LocalBookingsPage {

  private final Page page;
  private final MainConfig config;

  @Inject
  public LocalBookingsPage(Page page, MainConfig config) {
    this.page = page;
    this.config = config;
  }

  public void open() {
    page.navigate(config.localBookingBaseUrl() + "/app/bookings");
  }

  public Locator title() {
    return page.getByRole(com.microsoft.playwright.options.AriaRole.HEADING);
  }

  public Locator currentUser() {
    return page.getByTestId("current-user");
  }

  public Locator welcomeMessage() {
    return page.getByTestId("welcome-message");
  }

  public Locator authenticationError() {
    return page.getByTestId("auth-error");
  }
}
