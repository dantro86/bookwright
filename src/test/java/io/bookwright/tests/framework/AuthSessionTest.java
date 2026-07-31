package io.bookwright.tests.framework;

import io.bookwright.api.AuthSession;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class AuthSessionTest {

    @Test
    void exposesAuthenticationCookie() {
        assertThat(new AuthSession("abc123").cookie()).isEqualTo("token=abc123");
    }

    @Test
    void rejectsBlankToken() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new AuthSession(" "))
                .withMessage("Auth session token must not be blank");
    }
}
