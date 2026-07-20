package io.bookwright.ui;

import com.google.inject.Inject;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

/** Cart + two checkout steps of saucedemo, small enough to be one page object. */
public class CheckoutPage {

    private final Page page;

    private final Locator cartItems;
    private final Locator checkoutButton;
    private final Locator firstNameInput;
    private final Locator lastNameInput;
    private final Locator postalCodeInput;
    private final Locator continueButton;
    private final Locator finishButton;
    private final Locator completeHeader;

    @Inject
    public CheckoutPage(Page page) {
        this.page = page;
        this.cartItems = page.locator("[data-test=inventory-item]");
        this.checkoutButton = page.locator("[data-test=checkout]");
        this.firstNameInput = page.locator("[data-test=firstName]");
        this.lastNameInput = page.locator("[data-test=lastName]");
        this.postalCodeInput = page.locator("[data-test=postalCode]");
        this.continueButton = page.locator("[data-test=continue]");
        this.finishButton = page.locator("[data-test=finish]");
        this.completeHeader = page.locator("[data-test=complete-header]");
    }

    public Locator cartItems() {
        return cartItems;
    }

    public void startCheckout() {
        checkoutButton.click();
    }

    public void fillCustomerInfo(String firstName, String lastName, String zip) {
        firstNameInput.fill(firstName);
        lastNameInput.fill(lastName);
        postalCodeInput.fill(zip);
        continueButton.click();
    }

    public void finish() {
        finishButton.click();
    }

    public Locator completeHeader() {
        return completeHeader;
    }
}
