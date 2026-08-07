package io.bookwright.junit;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import io.bookwright.di.ApiModule;
import io.bookwright.di.DbModule;
import io.bookwright.di.UiModule;
import io.bookwright.fixtures.database.HotelDatabaseFixtures;
import io.bookwright.fixtures.local.LocalUserFixtures;
import io.bookwright.fixtures.saucedemo.SauceDemoFixtures;
import io.bookwright.steps.ApiSteps;
import io.bookwright.steps.DbSteps;
import io.bookwright.steps.UiSteps;
import io.bookwright.teardown.TeardownStorage;
import io.bookwright.ui.BrowserManager;
import java.util.Set;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolver;

/**
 * Hands tests ready-made Steps facades. One Guice injector per steps facade and test method, cached
 * in the method-scoped store so all injected objects share the same per-test teardown storage.
 */
public class StepsParameterResolver implements ParameterResolver {

  private static final Set<Class<?>> SUPPORTED =
      Set.of(ApiSteps.class, UiSteps.class, DbSteps.class);

  @Override
  public boolean supportsParameter(
      ParameterContext parameterContext, ExtensionContext extensionContext) {
    Class<?> type = parameterContext.getParameter().getType();
    return type == TestStore.class
        || type == TestUser.class
        || type == SauceDemoFixtures.class
        || type == LocalUserFixtures.class
        || type == HotelDatabaseFixtures.class
        || SUPPORTED.contains(type);
  }

  @Override
  public Object resolveParameter(
      ParameterContext parameterContext, ExtensionContext extensionContext) {
    Class<?> type = parameterContext.getParameter().getType();
    if (type == TestStore.class) {
      return new TestStore(extensionContext);
    }
    if (type == SauceDemoFixtures.class) {
      return SauceDemoFixtures.from(io.bookwright.config.Configs.main());
    }
    if (type == LocalUserFixtures.class) {
      return LocalUserFixtures.from(io.bookwright.config.Configs.main());
    }
    if (type == HotelDatabaseFixtures.class) {
      return HotelDatabaseFixtures.seeded();
    }
    if (type == TestUser.class) {
      return UserFixtureExtension.require(extensionContext);
    }
    if (type == UiSteps.class) {
      // Fresh browser context per UI test; closed when the method store closes
      NamespaceRegistry.methodStore(extensionContext)
          .getOrComputeIfAbsent(
              "browser-context-cleanup", key -> (AutoCloseable) BrowserManager::closeContext);
      // Browser and Playwright are reused within a class and closed after it.
      NamespaceRegistry.classStore(extensionContext)
          .getOrComputeIfAbsent(
              "browser-session-cleanup-" + Thread.currentThread().threadId(),
              key -> BrowserManager.sessionResource(),
              AutoCloseable.class);
    }
    return injectorFor(type, extensionContext).getInstance(type);
  }

  static Injector injectorFor(Class<?> stepsType, ExtensionContext context) {
    ExtensionContext.Store store = NamespaceRegistry.methodStore(context);
    return store.getOrComputeIfAbsent(
        "guice-injector-" + stepsType.getSimpleName(),
        key -> Guice.createInjector(moduleFor(stepsType, context)),
        Injector.class);
  }

  private static Module moduleFor(Class<?> stepsType, ExtensionContext context) {
    if (stepsType == ApiSteps.class) {
      return new ApiModule(TeardownStorage.getOrCreate(context));
    }
    if (stepsType == DbSteps.class) {
      return new DbModule(TeardownStorage.getOrCreate(context));
    }
    if (stepsType == UiSteps.class) {
      return new UiModule(context);
    }
    throw new IllegalArgumentException("Unsupported steps facade: " + stepsType.getName());
  }
}
