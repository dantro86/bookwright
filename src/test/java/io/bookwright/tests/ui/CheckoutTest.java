package io.bookwright.tests.ui;

import io.bookwright.annotations.OwnerDanil;
import io.bookwright.annotations.Regression;
import io.bookwright.annotations.Ui;
import io.bookwright.fixtures.saucedemo.SauceDemoFixtures;
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
  void standardUserCanCheckout(UiSteps ui, SauceDemoFixtures fixtures) {
    String product = fixtures.catalog().checkoutProduct();
    ui.sauceDemo().login().login(fixtures.standardUser());
    ui.sauceDemo().inventory().assertReady(fixtures.catalog());
    ui.sauceDemo().inventory().addToCart(product, fixtures.catalog());
    ui.sauceDemo().inventory().openCart();
    ui.sauceDemo().checkout().completeAndAssert(product, fixtures.checkout());
  }
}
