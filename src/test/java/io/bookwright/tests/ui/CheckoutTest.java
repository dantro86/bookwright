package io.bookwright.tests.ui;

import io.bookwright.annotations.OwnerDanil;
import io.bookwright.annotations.Regression;
import io.bookwright.annotations.Ui;
import io.bookwright.steps.UiSteps;
import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@Ui
@Regression
@OwnerDanil
@Feature("Checkout")
class CheckoutTest {

    @Test
    @DisplayName("Standard user can check out a backpack")
    void standardUserCanCheckout(UiSteps ui) {
        ui.loginAsStandardUser();
        ui.addToCart("Sauce Labs Backpack");
        ui.checkoutAndAssertOrderComplete();
    }
}
