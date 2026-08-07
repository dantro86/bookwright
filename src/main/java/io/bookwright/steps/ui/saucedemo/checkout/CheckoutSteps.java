package io.bookwright.steps.ui.saucedemo.checkout;

import com.google.inject.Inject;
import com.microsoft.playwright.assertions.PlaywrightAssertions;
import io.bookwright.ui.CheckoutPage;
import io.qameta.allure.Step;
import java.util.regex.Pattern;

public class CheckoutSteps {

  private final CheckoutPage page;

  @Inject
  public CheckoutSteps(CheckoutPage page) {
    this.page = page;
  }

  @Step("Check out Sauce Demo product '{productName}' and verify completion")
  public void completeAndAssert(String productName) {
    PlaywrightAssertions.assertThat(page.cartItems()).hasCount(1);
    PlaywrightAssertions.assertThat(page.itemNames()).hasText(new String[] {productName});
    page.startCheckout();
    page.fillCustomerInfo("Test", "Guest", "00100");
    PlaywrightAssertions.assertThat(page.cartItems()).hasCount(1);
    PlaywrightAssertions.assertThat(page.itemNames()).hasText(new String[] {productName});
    page.finish();
    PlaywrightAssertions.assertThat(page.completeHeader()).hasText("Thank you for your order!");
    PlaywrightAssertions.assertThat(page.completeText())
        .containsText("Your order has been dispatched");
    PlaywrightAssertions.assertThat(page.cartItems()).hasCount(0);
    PlaywrightAssertions.assertThat(page.page())
        .hasURL(Pattern.compile(".*/checkout-complete\\.html"));
  }
}
