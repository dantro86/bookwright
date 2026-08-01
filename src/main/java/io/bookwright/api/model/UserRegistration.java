package io.bookwright.api.model;

public record UserRegistration(String email, String password, String displayName) {

  @Override
  public String toString() {
    return "UserRegistration[email=%s, password=[REDACTED], displayName=%s]"
        .formatted(email, displayName);
  }
}
