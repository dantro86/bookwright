package io.bookwright.api.model;

import java.time.Instant;

public record UserSession(String accessToken, Instant expiresAt, UserProfile user) {

  public static final String COOKIE_NAME = "bookwright_session";

  public UserSession {
    if (accessToken == null || accessToken.isBlank()) {
      throw new IllegalArgumentException("User access token must not be blank");
    }
    if (expiresAt == null || user == null) {
      throw new IllegalArgumentException("User session expiry and profile are required");
    }
  }

  public String bearerToken() {
    return "Bearer " + accessToken;
  }

  @Override
  public String toString() {
    return "UserSession[accessToken=[REDACTED], expiresAt=%s, user=%s]".formatted(expiresAt, user);
  }
}
