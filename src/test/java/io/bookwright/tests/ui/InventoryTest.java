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
@Feature("Inventory")
class InventoryTest {

  @Test
  @DisplayName("Products can be sorted by name Z to A")
  void productsCanBeSortedDescending(UiSteps ui) {
    ui.sauceDemo().login().asStandardUser();
    ui.sauceDemo().inventory().assertReady();
    ui.sauceDemo().inventory().sortByNameDescAndAssertOrder();
  }

  @Test
  @DisplayName("Locked out user cannot log in")
  void lockedOutUserCannotLogIn(UiSteps ui) {
    ui.sauceDemo().login().asLockedOutUserAndExpectError();
  }

  @Test
  @DisplayName("Product with punctuation can be added by its visible name")
  void productWithPunctuationCanBeAddedByVisibleName(UiSteps ui) {
    ui.sauceDemo().login().asStandardUser();
    ui.sauceDemo().inventory().assertReady();
    ui.sauceDemo().inventory().addToCart("Test.allTheThings() T-Shirt (Red)");
  }
}
