package io.bookwright.steps;

import com.google.inject.Inject;
import com.microsoft.playwright.assertions.PlaywrightAssertions;
import io.bookwright.config.MainConfig;
import io.bookwright.ui.CheckoutPage;
import io.bookwright.ui.InventoryPage;
import io.bookwright.ui.LoginPage;
import io.qameta.allure.Step;

/**
 * Business-level UI flows over the page objects. Assertions use
 * Playwright's own auto-retrying assertion API.
 */
public class UiSteps {

    private final LoginPage loginPage;
    private final InventoryPage inventoryPage;
    private final CheckoutPage checkoutPage;
    private final MainConfig config;

    @Inject
    public UiSteps(LoginPage loginPage, InventoryPage inventoryPage, CheckoutPage checkoutPage, MainConfig config) {
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
        PlaywrightAssertions.assertThat(inventoryPage.itemNames().first())
                .hasText("Test.allTheThings() T-Shirt (Red)");
    }

    @Step("Add product '{productName}' to the cart")
    public void addToCart(String productName) {
        inventoryPage.addToCart(productName);
        PlaywrightAssertions.assertThat(inventoryPage.cartBadge()).hasText("1");
    }

    @Step("Check out and verify the order is complete")
    public void checkoutAndAssertOrderComplete() {
        inventoryPage.openCart();
        PlaywrightAssertions.assertThat(checkoutPage.cartItems()).hasCount(1);
        checkoutPage.startCheckout();
        checkoutPage.fillCustomerInfo("Test", "Guest", "00100");
        checkoutPage.finish();
        PlaywrightAssertions.assertThat(checkoutPage.completeHeader())
                .hasText("Thank you for your order!");
    }
}
