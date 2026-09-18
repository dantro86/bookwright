package io.bookwright.junit;

import java.util.Set;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolver;

/** Resolves framework-owned state; product steps and scenario fixtures have dedicated resolvers. */
public class FrameworkParameterResolver implements ParameterResolver {

  private static final Set<Class<?>> SUPPORTED = Set.of(TestStore.class, TestUser.class);

  @Override
  public boolean supportsParameter(
      ParameterContext parameterContext, ExtensionContext extensionContext) {
    return SUPPORTED.contains(parameterContext.getParameter().getType());
  }

  @Override
  public Object resolveParameter(
      ParameterContext parameterContext, ExtensionContext extensionContext) {
    Class<?> type = parameterContext.getParameter().getType();
    if (type == TestStore.class) {
      return new TestStore(extensionContext);
    }
    return UserFixtureExtension.require(extensionContext);
  }
}
