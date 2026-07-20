package io.bookwright.junit;

import lombok.experimental.UtilityClass;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * Central registry of JUnit store namespaces and keys, so extensions and tests
 * agree on where shared state lives. Class scope = reusable per test class
 * (injectors); method scope = isolated per test (auth session, precondition data).
 */
@UtilityClass
public class NamespaceRegistry {

    public static final String AUTH_TOKEN_KEY = "authToken";
    public static final String BOOKING_KEY = "createdBooking";

    public ExtensionContext.Store classStore(ExtensionContext context) {
        Class<?> testClass = context.getRequiredTestClass();
        return context.getStore(ExtensionContext.Namespace.create(NamespaceRegistry.class, testClass));
    }

    public ExtensionContext.Store methodStore(ExtensionContext context) {
        return context.getStore(ExtensionContext.Namespace.create(
                NamespaceRegistry.class, context.getRequiredTestClass(), context.getRequiredTestMethod()));
    }
}
