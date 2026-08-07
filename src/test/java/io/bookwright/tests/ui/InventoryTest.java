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
@Feature("Inventory")
class InventoryTest {

  @Test
  @DisplayName("Products can be sorted by name Z to A")
  void productsCanBeSortedDescending(UiSteps ui, SauceDemoFixtures fixtures) {
    ui.sauceDemo().login().login(fixtures.standardUser());
    ui.sauceDemo().inventory().assertReady(fixtures.catalog());
    ui.sauceDemo().inventory().sortByNameDescAndAssertOrder(fixtures.catalog());
  }

  @Test
  @DisplayName("Locked out user cannot log in")
  void lockedOutUserCannotLogIn(UiSteps ui, SauceDemoFixtures fixtures) {
    ui.sauceDemo().login().loginAndExpectError(fixtures.lockedOut());
  }

  @Test
  @DisplayName("Product with punctuation can be added by its visible name")
  void productWithPunctuationCanBeAddedByVisibleName(UiSteps ui, SauceDemoFixtures fixtures) {
    ui.sauceDemo().login().login(fixtures.standardUser());
    ui.sauceDemo().inventory().assertReady(fixtures.catalog());
    ui.sauceDemo()
        .inventory()
        .addToCart(fixtures.catalog().punctuationProduct(), fixtures.catalog());
  }
}
