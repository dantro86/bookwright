package io.bookwright.steps.ui.saucedemo.inventory;

import com.google.inject.Inject;
import com.microsoft.playwright.assertions.PlaywrightAssertions;
import io.bookwright.fixtures.saucedemo.SauceDemoFixtures.Catalog;
import io.bookwright.ui.InventoryPage;
import io.qameta.allure.Step;

public class InventorySteps {

  private final InventoryPage page;

  @Inject
  public InventorySteps(InventoryPage page) {
    this.page = page;
  }

  @Step("Verify the Sauce Demo inventory is ready")
  public void assertReady(Catalog expected) {
    PlaywrightAssertions.assertThat(page.title()).hasText(expected.title());
    PlaywrightAssertions.assertThat(page.inventoryItems()).hasCount(expected.products().size());
    PlaywrightAssertions.assertThat(page.itemNames())
        .hasText(expected.products().toArray(String[]::new));
    PlaywrightAssertions.assertThat(page.cartLink()).isVisible();
  }

  @Step("Sort Sauce Demo products by name Z to A and verify order")
  public void sortByNameDescAndAssertOrder(Catalog expected) {
    page.sortBy(expected.descendingSortValue());
    PlaywrightAssertions.assertThat(page.sortSelect()).hasValue(expected.descendingSortValue());
    PlaywrightAssertions.assertThat(page.itemNames())
        .hasText(expected.descendingProducts().toArray(String[]::new));
  }

  @Step("Add Sauce Demo product '{productName}' to the cart")
  public void addToCart(String productName, Catalog expected) {
    page.addToCart(productName);
    PlaywrightAssertions.assertThat(page.cartBadge())
        .hasText(Integer.toString(expected.cartCountAfterSingleAdd()));
    PlaywrightAssertions.assertThat(page.productActionButton(productName))
        .hasText(expected.removeButtonText());
  }

  @Step("Open the Sauce Demo cart")
  public void openCart() {
    page.openCart();
  }
}
