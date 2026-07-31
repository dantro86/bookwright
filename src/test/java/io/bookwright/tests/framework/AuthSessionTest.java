package io.bookwright.tests.framework;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import io.bookwright.api.AuthSession;
import org.junit.jupiter.api.Test;

class AuthSessionTest {

  @Test
  void exposesAuthenticationCookie() {
    assertThat(new AuthSession("abc123").cookie()).isEqualTo("token=abc123");
  }

  @Test
  void doesNotExposeTokenThroughStringRepresentation() {
    assertThat(new AuthSession("abc123").toString())
        .isEqualTo("AuthSession[token=[REDACTED]]")
        .doesNotContain("abc123");
  }

  @Test
  void rejectsBlankToken() {
    assertThatIllegalArgumentException()
        .isThrownBy(() -> new AuthSession(" "))
        .withMessage("Auth session token must not be blank");
  }
}
