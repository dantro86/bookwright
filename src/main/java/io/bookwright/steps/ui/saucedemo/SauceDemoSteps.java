package io.bookwright.steps.ui.saucedemo;

import com.google.inject.Inject;
import io.bookwright.steps.ui.saucedemo.checkout.CheckoutSteps;
import io.bookwright.steps.ui.saucedemo.inventory.InventorySteps;
import io.bookwright.steps.ui.saucedemo.login.LoginSteps;
import lombok.Getter;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
public class SauceDemoSteps {
  @Inject private LoginSteps login;
  @Inject private InventorySteps inventory;
  @Inject private CheckoutSteps checkout;
}
