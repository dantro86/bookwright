package io.bookwright.junit;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import io.bookwright.di.ApiModule;
import io.bookwright.di.DbModule;
import io.bookwright.di.UiModule;
import io.bookwright.steps.ApiSteps;
import io.bookwright.steps.DbSteps;
import io.bookwright.steps.UiSteps;
import io.bookwright.ui.BrowserManager;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolver;

/**
 * Hands tests ready-made Steps facades. One Guice injector per test class,
 * cached in the class-scoped store so parallel classes stay independent.
 */
public class StepsParameterResolver implements ParameterResolver {

    private static final Map<Class<?>, List<Module>> SUPPORTED = Map.of(
            ApiSteps.class, List.of(new ApiModule()),
            UiSteps.class, List.of(new UiModule()),
            DbSteps.class, List.of(new DbModule())
    );

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
        Class<?> type = parameterContext.getParameter().getType();
        return type == TestStore.class || SUPPORTED.containsKey(type);
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
        Class<?> type = parameterContext.getParameter().getType();
        if (type == TestStore.class) {
            return new TestStore(extensionContext);
        }
        if (type == UiSteps.class) {
            // Fresh browser context per UI test; closed when the method store closes
            NamespaceRegistry.methodStore(extensionContext).getOrComputeIfAbsent(
                    "browser-context-cleanup",
                    key -> (ExtensionContext.Store.CloseableResource) BrowserManager::closeContext);
        }
        return injectorFor(type, extensionContext).getInstance(type);
    }

    static Injector injectorFor(Class<?> stepsType, ExtensionContext context) {
        ExtensionContext.Store store = NamespaceRegistry.classStore(context);
        return store.getOrComputeIfAbsent(
                "guice-injector-" + stepsType.getSimpleName(),
                key -> Guice.createInjector(SUPPORTED.get(stepsType)),
                Injector.class);
    }
}
