package io.bookwright.fixtures.saucedemo;

import io.bookwright.config.MainConfig;
import java.util.List;
import java.util.regex.Pattern;

/** Typed, immutable Sauce Demo accounts, catalog, and scenario expectations. */
public record SauceDemoFixtures(
    User standardUser,
    LoginCase invalidPassword,
    LoginCase lockedOut,
    Catalog catalog,
    Checkout checkout) {

  public static SauceDemoFixtures from(MainConfig config) {
    User standard = new User(config.uiUser(), config.uiPassword());
    List<String> products =
        List.of(
            "Sauce Labs Backpack",
            "Sauce Labs Bike Light",
            "Sauce Labs Bolt T-Shirt",
            "Sauce Labs Fleece Jacket",
            "Sauce Labs Onesie",
            "Test.allTheThings() T-Shirt (Red)");
    return new SauceDemoFixtures(
        standard,
        new LoginCase(
            new User(config.uiUser(), "definitely-wrong"), "Username and password do not match"),
        new LoginCase(
            new User("locked_out_user", config.uiPassword()),
            "Sorry, this user has been locked out"),
        new Catalog(
            "Products",
            products,
            "za",
            products.reversed(),
            "Sauce Labs Backpack",
            "Test.allTheThings() T-Shirt (Red)",
            "Remove",
            1),
        new Checkout(
            new Customer("Test", "Guest", "00100"),
            "Thank you for your order!",
            "Your order has been dispatched",
            Pattern.compile(".*/checkout-complete\\.html"),
            1,
            0));
  }

  public record User(String username, String password) {
    @Override
    public String toString() {
      return "User[username=%s, password=[REDACTED]]".formatted(username);
    }
  }

  public record LoginCase(User user, String expectedError) {}

  public record Catalog(
      String title,
      List<String> products,
      String descendingSortValue,
      List<String> descendingProducts,
      String checkoutProduct,
      String punctuationProduct,
      String removeButtonText,
      int cartCountAfterSingleAdd) {
    public Catalog {
      products = List.copyOf(products);
      descendingProducts = List.copyOf(descendingProducts);
    }
  }

  public record Customer(String firstName, String lastName, String postalCode) {}

  public record Checkout(
      Customer customer,
      String completeHeader,
      String completeText,
      Pattern completeUrl,
      int overviewItemCount,
      int completedCartItemCount) {}
}
