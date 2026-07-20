package io.bookwright.tests.ui;

import io.bookwright.annotations.OwnerDanil;
import io.bookwright.annotations.Smoke;
import io.bookwright.annotations.Ui;
import io.bookwright.steps.UiSteps;
import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@Ui
@Smoke
@OwnerDanil
@Feature("Login")
class LoginTest {

    @Test
    @DisplayName("Standard user can log in")
    void standardUserCanLogIn(UiSteps ui) {
        ui.loginAsStandardUser();
    }

    @Test
    @DisplayName("Invalid password shows an error")
    void invalidPasswordShowsError(UiSteps ui) {
        ui.loginWithInvalidPasswordAndExpectError();
    }
}
