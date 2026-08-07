package io.bookwright.fixtures.local;

import io.bookwright.api.model.UserCredentials;
import io.bookwright.config.MainConfig;

/** Stable local-user scenarios derived from stand configuration. */
public record LocalUserFixtures(UserCredentials invalidExistingUser, UiExpectations ui) {

  public static LocalUserFixtures from(MainConfig config) {
    return new LocalUserFixtures(
        new UserCredentials(config.localExistingUserEmail(), "incorrect-password"),
        new UiExpectations(
            "Bookings",
            "Welcome, %s",
            "Authentication required",
            "Session is missing, invalid, or expired"));
  }

  public record UiExpectations(
      String authenticatedTitle,
      String welcomeTemplate,
      String authenticationRequiredTitle,
      String authenticationError) {
    public String welcomeFor(String displayName) {
      return welcomeTemplate.formatted(displayName);
    }
  }
}
