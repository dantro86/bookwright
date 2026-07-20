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
        ui.loginAsStandardUser();
        ui.sortByNameDescAndAssertOrder();
    }

    @Test
    @DisplayName("Locked out user cannot log in")
    void lockedOutUserCannotLogIn(UiSteps ui) {
        ui.loginAsLockedOutUserAndExpectError();
    }
}
