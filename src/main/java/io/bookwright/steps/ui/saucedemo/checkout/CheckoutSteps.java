package io.bookwright.steps.ui.saucedemo.checkout;

import com.google.inject.Inject;
import com.microsoft.playwright.assertions.PlaywrightAssertions;
import io.bookwright.fixtures.saucedemo.SauceDemoFixtures.Checkout;
import io.bookwright.ui.CheckoutPage;
import io.qameta.allure.Step;

public class CheckoutSteps {

  private final CheckoutPage page;

  @Inject
  public CheckoutSteps(CheckoutPage page) {
    this.page = page;
  }

  @Step("Check out Sauce Demo product '{productName}' and verify completion")
  public void completeAndAssert(String productName, Checkout expected) {
    PlaywrightAssertions.assertThat(page.cartItems()).hasCount(expected.overviewItemCount());
    PlaywrightAssertions.assertThat(page.itemNames()).hasText(new String[] {productName});
    page.startCheckout();
    page.fillCustomerInfo(
        expected.customer().firstName(),
        expected.customer().lastName(),
        expected.customer().postalCode());
    PlaywrightAssertions.assertThat(page.cartItems()).hasCount(expected.overviewItemCount());
    PlaywrightAssertions.assertThat(page.itemNames()).hasText(new String[] {productName});
    page.finish();
    PlaywrightAssertions.assertThat(page.completeHeader()).hasText(expected.completeHeader());
    PlaywrightAssertions.assertThat(page.completeText()).containsText(expected.completeText());
    PlaywrightAssertions.assertThat(page.cartItems()).hasCount(expected.completedCartItemCount());
    PlaywrightAssertions.assertThat(page.page()).hasURL(expected.completeUrl());
  }
}
