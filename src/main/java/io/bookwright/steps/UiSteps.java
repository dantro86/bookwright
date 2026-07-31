package io.bookwright.steps;

import com.google.inject.Inject;
import com.microsoft.playwright.assertions.PlaywrightAssertions;
import io.bookwright.config.MainConfig;
import io.bookwright.ui.CheckoutPage;
import io.bookwright.ui.InventoryPage;
import io.bookwright.ui.LoginPage;
import io.qameta.allure.Step;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Business-level UI flows over the page objects. Assertions use Playwright's own auto-retrying
 * assertion API.
 */
public class UiSteps {

  private static final List<String> DEFAULT_PRODUCT_ORDER =
      List.of(
          "Sauce Labs Backpack",
          "Sauce Labs Bike Light",
          "Sauce Labs Bolt T-Shirt",
          "Sauce Labs Fleece Jacket",
          "Sauce Labs Onesie",
          "Test.allTheThings() T-Shirt (Red)");

  private static final List<String> PRODUCT_ORDER_Z_TO_A = DEFAULT_PRODUCT_ORDER.reversed();

  private final LoginPage loginPage;
  private final InventoryPage inventoryPage;
  private final CheckoutPage checkoutPage;
  private final MainConfig config;

  @Inject
  public UiSteps(
      LoginPage loginPage,
      InventoryPage inventoryPage,
      CheckoutPage checkoutPage,
      MainConfig config) {
    this.loginPage = loginPage;
    this.inventoryPage = inventoryPage;
    this.checkoutPage = checkoutPage;
    this.config = config;
  }

  @Step("Log in as the configured standard user")
  public void loginAsStandardUser() {
    loginPage.open();
    loginPage.login(config.uiUser(), config.uiPassword());
    PlaywrightAssertions.assertThat(inventoryPage.title()).hasText("Products");
    PlaywrightAssertions.assertThat(inventoryPage.inventoryItems())
        .hasCount(DEFAULT_PRODUCT_ORDER.size());
    PlaywrightAssertions.assertThat(inventoryPage.itemNames())
        .hasText(DEFAULT_PRODUCT_ORDER.toArray(String[]::new));
    PlaywrightAssertions.assertThat(inventoryPage.cartLink()).isVisible();
  }

  @Step("Log in with invalid password and expect an error")
  public void loginWithInvalidPasswordAndExpectError() {
    loginPage.open();
    loginPage.login(config.uiUser(), "definitely-wrong");
    PlaywrightAssertions.assertThat(loginPage.errorMessage())
        .containsText("Username and password do not match");
  }

  @Step("Log in as locked out user and expect an error")
  public void loginAsLockedOutUserAndExpectError() {
    loginPage.open();
    loginPage.login("locked_out_user", config.uiPassword());
    PlaywrightAssertions.assertThat(loginPage.errorMessage())
        .containsText("Sorry, this user has been locked out");
  }

  @Step("Sort products by name Z to A and verify order")
  public void sortByNameDescAndAssertOrder() {
    inventoryPage.sortBy("za");
    PlaywrightAssertions.assertThat(inventoryPage.sortSelect()).hasValue("za");
    PlaywrightAssertions.assertThat(inventoryPage.itemNames())
        .hasText(PRODUCT_ORDER_Z_TO_A.toArray(String[]::new));
  }

  @Step("Add product '{productName}' to the cart")
  public void addToCart(String productName) {
    inventoryPage.addToCart(productName);
    PlaywrightAssertions.assertThat(inventoryPage.cartBadge()).hasText("1");
    PlaywrightAssertions.assertThat(inventoryPage.productActionButton(productName))
        .hasText("Remove");
  }

  @Step("Check out '{productName}' and verify the order is complete")
  public void checkoutAndAssertOrderComplete(String productName) {
    inventoryPage.openCart();
    PlaywrightAssertions.assertThat(checkoutPage.cartItems()).hasCount(1);
    PlaywrightAssertions.assertThat(checkoutPage.itemNames()).hasText(new String[] {productName});
    checkoutPage.startCheckout();
    checkoutPage.fillCustomerInfo("Test", "Guest", "00100");
    PlaywrightAssertions.assertThat(checkoutPage.cartItems()).hasCount(1);
    PlaywrightAssertions.assertThat(checkoutPage.itemNames()).hasText(new String[] {productName});
    checkoutPage.finish();
    PlaywrightAssertions.assertThat(checkoutPage.completeHeader())
        .hasText("Thank you for your order!");
    PlaywrightAssertions.assertThat(checkoutPage.completeText())
        .containsText("Your order has been dispatched");
    PlaywrightAssertions.assertThat(checkoutPage.cartItems()).hasCount(0);
    PlaywrightAssertions.assertThat(checkoutPage.page())
        .hasURL(Pattern.compile(".*/checkout-complete\\.html"));
  }
}
