package io.bookwright.api;

/** Explicit authenticated API session passed to operations that require authorization. */
public record AuthSession(String token) {

  public AuthSession {
    if (token == null || token.isBlank()) {
      throw new IllegalArgumentException("Auth session token must not be blank");
    }
  }

  public String cookie() {
    return "token=" + token;
  }

  @Override
  public String toString() {
    return "AuthSession[token=[REDACTED]]";
  }
}
