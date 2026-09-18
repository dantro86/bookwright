package io.bookwright.junit;

import io.bookwright.steps.ApiSteps;
import io.bookwright.steps.DbSteps;
import io.bookwright.steps.UiSteps;
import io.bookwright.ui.BrowserManager;
import java.util.Set;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolver;

/**
 * Hands tests ready-made step facades from one method-scoped runtime. Framework state and fixtures
 * are intentionally resolved by separate extensions.
 */
public class StepsParameterResolver implements ParameterResolver {

  private static final Set<Class<?>> STEP_TYPES =
      Set.of(ApiSteps.class, UiSteps.class, DbSteps.class);

  @Override
  public boolean supportsParameter(
      ParameterContext parameterContext, ExtensionContext extensionContext) {
    return STEP_TYPES.contains(parameterContext.getParameter().getType());
  }

  @Override
  public Object resolveParameter(
      ParameterContext parameterContext, ExtensionContext extensionContext) {
    Class<?> type = parameterContext.getParameter().getType();
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
    return TestRuntime.resolve(type, extensionContext);
  }
}
