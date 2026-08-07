package io.bookwright.steps;

import com.google.inject.Inject;
import io.bookwright.steps.ui.local.LocalUiSteps;
import io.bookwright.steps.ui.saucedemo.SauceDemoSteps;
import lombok.Getter;
import lombok.experimental.Accessors;

/** Compact entry point exposing target-owned UI domains. */
@Getter
@Accessors(fluent = true)
public class UiSteps {
  @Inject private SauceDemoSteps sauceDemo;
  @Inject private LocalUiSteps local;
}
