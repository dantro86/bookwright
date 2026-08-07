package io.bookwright.steps.ui.saucedemo.inventory;

import com.google.inject.Inject;
import com.microsoft.playwright.assertions.PlaywrightAssertions;
import io.bookwright.ui.InventoryPage;
import io.qameta.allure.Step;
import java.util.List;

public class InventorySteps {

  private static final List<String> DEFAULT_PRODUCT_ORDER =
      List.of(
          "Sauce Labs Backpack",
          "Sauce Labs Bike Light",
          "Sauce Labs Bolt T-Shirt",
          "Sauce Labs Fleece Jacket",
          "Sauce Labs Onesie",
          "Test.allTheThings() T-Shirt (Red)");

  private final InventoryPage page;

  @Inject
  public InventorySteps(InventoryPage page) {
    this.page = page;
  }

  @Step("Verify the Sauce Demo inventory is ready")
  public void assertReady() {
    PlaywrightAssertions.assertThat(page.title()).hasText("Products");
    PlaywrightAssertions.assertThat(page.inventoryItems()).hasCount(DEFAULT_PRODUCT_ORDER.size());
    PlaywrightAssertions.assertThat(page.itemNames())
        .hasText(DEFAULT_PRODUCT_ORDER.toArray(String[]::new));
    PlaywrightAssertions.assertThat(page.cartLink()).isVisible();
  }

  @Step("Sort Sauce Demo products by name Z to A and verify order")
  public void sortByNameDescAndAssertOrder() {
    page.sortBy("za");
    PlaywrightAssertions.assertThat(page.sortSelect()).hasValue("za");
    PlaywrightAssertions.assertThat(page.itemNames())
        .hasText(DEFAULT_PRODUCT_ORDER.reversed().toArray(String[]::new));
  }

  @Step("Add Sauce Demo product '{productName}' to the cart")
  public void addToCart(String productName) {
    page.addToCart(productName);
    PlaywrightAssertions.assertThat(page.cartBadge()).hasText("1");
    PlaywrightAssertions.assertThat(page.productActionButton(productName)).hasText("Remove");
  }

  @Step("Open the Sauce Demo cart")
  public void openCart() {
    page.openCart();
  }
}
