package io.bookwright.steps.ui.local.bookings;

import com.google.inject.Inject;
import com.microsoft.playwright.assertions.PlaywrightAssertions;
import io.bookwright.junit.TestUser;
import io.bookwright.ui.LocalBookingsPage;
import io.qameta.allure.Step;

public class BookingSteps {

  private final LocalBookingsPage page;

  @Inject
  public BookingSteps(LocalBookingsPage page) {
    this.page = page;
  }

  @Step("Open the local bookings UI as API-authenticated user {user.profile.email}")
  public void openAs(TestUser user) {
    page.open();
    PlaywrightAssertions.assertThat(page.title()).hasText("Bookings");
    PlaywrightAssertions.assertThat(page.currentUser()).hasText(user.profile().email());
    PlaywrightAssertions.assertThat(page.welcomeMessage())
        .hasText("Welcome, " + user.profile().displayName());
  }

  @Step("Open the local bookings UI without a session")
  public void openAndExpectAuthenticationRequired() {
    page.open();
    PlaywrightAssertions.assertThat(page.title()).hasText("Authentication required");
    PlaywrightAssertions.assertThat(page.authenticationError())
        .containsText("Session is missing, invalid, or expired");
  }
}
