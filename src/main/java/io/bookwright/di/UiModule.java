package io.bookwright.di;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.microsoft.playwright.Page;
import io.bookwright.config.Configs;
import io.bookwright.config.MainConfig;
import io.bookwright.junit.NamespaceRegistry;
import io.bookwright.junit.TestUser;
import io.bookwright.ui.BrowserManager;
import org.junit.jupiter.api.extension.ExtensionContext;

public class UiModule extends AbstractModule {

  private final ExtensionContext context;

  public UiModule(ExtensionContext context) {
    this.context = context;
  }

  @Override
  protected void configure() {
    bind(MainConfig.class).toInstance(Configs.main());
  }

  @Provides
  Page page() {
    TestUser user =
        NamespaceRegistry.methodStore(context).get(NamespaceRegistry.TEST_USER_KEY, TestUser.class);
    return user == null
        ? BrowserManager.page()
        : BrowserManager.page(user.session(), Configs.main().localBookingBaseUrl());
  }
}
