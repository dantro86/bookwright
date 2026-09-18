package io.bookwright.junit;

import com.google.inject.ConfigurationException;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

/** Resolves explicitly marked fixtures through constructor injection without a central catalog. */
public class TestFixtureParameterResolver implements ParameterResolver {

  @Override
  public boolean supportsParameter(
      ParameterContext parameterContext, ExtensionContext extensionContext) {
    return parameterContext.isAnnotated(TestFixture.class);
  }

  @Override
  public Object resolveParameter(
      ParameterContext parameterContext, ExtensionContext extensionContext) {
    Class<?> type = parameterContext.getParameter().getType();
    try {
      return TestRuntime.resolve(type, extensionContext);
    } catch (ConfigurationException exception) {
      throw new ParameterResolutionException(
          "@TestFixture parameter %s must expose an injectable constructor"
              .formatted(type.getName()),
          exception);
    }
  }
}
