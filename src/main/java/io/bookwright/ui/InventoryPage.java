package io.bookwright.ui;

import com.google.inject.Inject;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

public class InventoryPage {

    private final Page page;

    private final Locator title;
    private final Locator cartBadge;
    private final Locator cartLink;
    private final Locator sortSelect;
    private final Locator itemNames;

    @Inject
    public InventoryPage(Page page) {
        this.page = page;
        this.title = page.locator("[data-test=title]");
        this.cartBadge = page.locator("[data-test=shopping-cart-badge]");
        this.cartLink = page.locator("[data-test=shopping-cart-link]");
        this.sortSelect = page.locator("[data-test=product-sort-container]");
        this.itemNames = page.locator("[data-test=inventory-item-name]");
    }

    public void sortBy(String optionValue) {
        sortSelect.selectOption(optionValue);
    }

    public Locator itemNames() {
        return itemNames;
    }

    public Locator title() {
        return title;
    }

    public Locator cartBadge() {
        return cartBadge;
    }

    public void addToCart(String productName) {
        String slug = productName.toLowerCase().replace(' ', '-');
        page.locator("[data-test=add-to-cart-" + slug + "]").click();
    }

    public void openCart() {
        cartLink.click();
    }
}
