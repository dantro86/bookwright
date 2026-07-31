package io.bookwright.ui;

import com.google.inject.Inject;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

public class InventoryPage {

    private final Locator title;
    private final Locator cartBadge;
    private final Locator cartLink;
    private final Locator sortSelect;
    private final Locator inventoryItems;
    private final Locator itemNames;

    @Inject
    public InventoryPage(Page page) {
        this.title = page.locator("[data-test=title]");
        this.cartBadge = page.locator("[data-test=shopping-cart-badge]");
        this.cartLink = page.locator("[data-test=shopping-cart-link]");
        this.sortSelect = page.locator("[data-test=product-sort-container]");
        this.inventoryItems = page.locator("[data-test=inventory-item]");
        this.itemNames = page.locator("[data-test=inventory-item-name]");
    }

    public void sortBy(String optionValue) {
        sortSelect.selectOption(optionValue);
    }

    public Locator itemNames() {
        return itemNames;
    }

    public Locator inventoryItems() {
        return inventoryItems;
    }

    public Locator sortSelect() {
        return sortSelect;
    }

    public Locator title() {
        return title;
    }

    public Locator cartBadge() {
        return cartBadge;
    }

    public Locator cartLink() {
        return cartLink;
    }

    public void addToCart(String productName) {
        productActionButton(productName).click();
    }

    public Locator productActionButton(String productName) {
        return productCard(productName).locator("button");
    }

    public void openCart() {
        cartLink.click();
    }

    private Locator productCard(String productName) {
        return inventoryItems.filter(new Locator.FilterOptions().setHasText(productName));
    }
}
