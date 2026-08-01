package io.bookwright.api.model;

public record UserCredentials(String email, String password) {

  public UserCredentials {
    if (email == null || email.isBlank()) {
      throw new IllegalArgumentException("User email must not be blank");
    }
    if (password == null || password.isBlank()) {
      throw new IllegalArgumentException("User password must not be blank");
    }
    email = email.trim().toLowerCase(java.util.Locale.ROOT);
  }

  @Override
  public String toString() {
    return "UserCredentials[email=%s, password=[REDACTED]]".formatted(email);
  }
}
