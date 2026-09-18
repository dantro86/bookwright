package io.bookwright.fixtures.saucedemo;

import com.google.inject.Inject;
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

  @Inject
  public SauceDemoFixtures(MainConfig config) {
    this(
        standardUser(config),
        invalidPassword(config),
        lockedOut(config),
        catalogFixtures(),
        checkoutFixtures());
  }

  public static SauceDemoFixtures from(MainConfig config) {
    return new SauceDemoFixtures(config);
  }

  private static User standardUser(MainConfig config) {
    return new User(config.uiUser(), config.uiPassword());
  }

  private static LoginCase invalidPassword(MainConfig config) {
    return new LoginCase(
        new User(config.uiUser(), "definitely-wrong"), "Username and password do not match");
  }

  private static LoginCase lockedOut(MainConfig config) {
    return new LoginCase(
        new User("locked_out_user", config.uiPassword()), "Sorry, this user has been locked out");
  }

  private static Catalog catalogFixtures() {
    List<String> products =
        List.of(
            "Sauce Labs Backpack",
            "Sauce Labs Bike Light",
            "Sauce Labs Bolt T-Shirt",
            "Sauce Labs Fleece Jacket",
            "Sauce Labs Onesie",
            "Test.allTheThings() T-Shirt (Red)");
    return new Catalog(
        "Products",
        products,
        "za",
        products.reversed(),
        "Sauce Labs Backpack",
        "Test.allTheThings() T-Shirt (Red)",
        "Remove",
        1);
  }

  private static Checkout checkoutFixtures() {
    return new Checkout(
        new Customer("Test", "Guest", "00100"),
        "Thank you for your order!",
        "Your order has been dispatched",
        Pattern.compile(".*/checkout-complete\\.html"),
        1,
        0);
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
