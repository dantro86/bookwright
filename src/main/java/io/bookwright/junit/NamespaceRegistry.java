package io.bookwright.junit;

import lombok.experimental.UtilityClass;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * Creates class- and method-scoped JUnit namespaces. State keys belong to the extension or storage
 * component that owns the corresponding value.
 */
@UtilityClass
public class NamespaceRegistry {

  public ExtensionContext.Store classStore(ExtensionContext context) {
    Class<?> testClass = context.getRequiredTestClass();
    ExtensionContext classContext = context;
    while (classContext.getTestMethod().isPresent()) {
      classContext =
          classContext
              .getParent()
              .orElseThrow(() -> new IllegalStateException("Test class context is not available"));
    }
    return classContext.getStore(
        ExtensionContext.Namespace.create(NamespaceRegistry.class, testClass));
  }

  public ExtensionContext.Store methodStore(ExtensionContext context) {
    return context.getStore(
        ExtensionContext.Namespace.create(
            NamespaceRegistry.class,
            context.getRequiredTestClass(),
            context.getRequiredTestMethod()));
  }
}
