package io.bookwright.tests.framework;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.bookwright.api.model.UserCredentials;
import io.bookwright.api.model.UserProfile;
import io.bookwright.api.model.UserRegistration;
import io.bookwright.api.model.UserSession;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class UserSessionTest {

  @Test
  void credentialsAndSessionDoNotExposeSecrets() {
    UserCredentials credentials = new UserCredentials("User@Example.com", "password-secret");
    UserSession session =
        new UserSession(
            "token-secret",
            Instant.parse("2030-01-01T00:00:00Z"),
            new UserProfile(7, credentials.email(), "Test User"));
    UserRegistration registration =
        new UserRegistration(credentials.email(), "registration-secret", "Test User");

    assertThat(credentials.email()).isEqualTo("user@example.com");
    assertThat(credentials.toString()).doesNotContain("password-secret").contains("[REDACTED]");
    assertThat(session.bearerToken()).isEqualTo("Bearer token-secret");
    assertThat(session.toString()).doesNotContain("token-secret").contains("[REDACTED]");
    assertThat(registration.toString())
        .doesNotContain("registration-secret")
        .contains("[REDACTED]");
  }

  @Test
  void blankSecretsAreRejected() {
    assertThatThrownBy(() -> new UserCredentials("user@example.com", " "))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(
            () ->
                new UserSession(
                    " ",
                    Instant.parse("2030-01-01T00:00:00Z"),
                    new UserProfile(7, "user@example.com", "Test User")))
        .isInstanceOf(IllegalArgumentException.class);
  }
}
