package io.bookwright.junit;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import io.bookwright.config.Configs;
import io.bookwright.config.MainConfig;
import io.bookwright.di.ApiModule;
import io.bookwright.di.DbModule;
import io.bookwright.di.UiModule;
import io.bookwright.teardown.TeardownStorage;
import io.bookwright.util.TestData;
import lombok.experimental.UtilityClass;
import org.junit.jupiter.api.extension.ExtensionContext;

/** Method-scoped composition root shared by steps, fixtures, and JUnit extensions. */
@UtilityClass
final class TestRuntime {

  private static final String INJECTOR_KEY = "testRuntimeInjector";

  Injector injector(ExtensionContext context) {
    return NamespaceRegistry.methodStore(context)
        .getOrComputeIfAbsent(
            INJECTOR_KEY,
            ignored ->
                Guice.createInjector(
                    new RuntimeModule(context), new ApiModule(), new DbModule(), new UiModule()),
            Injector.class);
  }

  <T> T resolve(Class<T> type, ExtensionContext context) {
    return injector(context).getInstance(type);
  }

  private static final class RuntimeModule extends AbstractModule {

    private final ExtensionContext context;

    private RuntimeModule(ExtensionContext context) {
      this.context = context;
    }

    @Override
    protected void configure() {
      bind(ExtensionContext.class).toInstance(context);
      bind(MainConfig.class).toInstance(Configs.main());
      bind(TestData.class).toInstance(TestDataExtension.getOrCreate(context));
      bind(TeardownStorage.class).toInstance(TeardownStorage.getOrCreate(context));
    }
  }
}
