package io.bookwright.steps.ui.local.bookings;

import com.google.inject.Inject;
import com.microsoft.playwright.assertions.PlaywrightAssertions;
import io.bookwright.fixtures.local.LocalUserFixtures.UiExpectations;
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
  public void openAs(TestUser user, UiExpectations expected) {
    page.open();
    PlaywrightAssertions.assertThat(page.title()).hasText(expected.authenticatedTitle());
    PlaywrightAssertions.assertThat(page.currentUser()).hasText(user.profile().email());
    PlaywrightAssertions.assertThat(page.welcomeMessage())
        .hasText(expected.welcomeFor(user.profile().displayName()));
  }

  @Step("Open the local bookings UI without a session")
  public void openAndExpectAuthenticationRequired(UiExpectations expected) {
    page.open();
    PlaywrightAssertions.assertThat(page.title()).hasText(expected.authenticationRequiredTitle());
    PlaywrightAssertions.assertThat(page.authenticationError())
        .containsText(expected.authenticationError());
  }
}
