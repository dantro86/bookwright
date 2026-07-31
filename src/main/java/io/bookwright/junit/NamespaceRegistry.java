package io.bookwright.junit;

import lombok.experimental.UtilityClass;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * Central registry of JUnit store namespaces and keys, so extensions and tests
 * agree on where shared state lives. Class scope = reusable resources;
 * method scope = isolated per test (injectors, auth session, preconditions, teardown).
 */
@UtilityClass
public class NamespaceRegistry {

    public static final String AUTH_SESSION_KEY = "authSession";
    public static final String BOOKING_KEY = "createdBooking";
    public static final String TEARDOWN_STORAGE_KEY = "teardownStorage";

    public ExtensionContext.Store classStore(ExtensionContext context) {
        Class<?> testClass = context.getRequiredTestClass();
        ExtensionContext classContext = context;
        while (classContext.getTestMethod().isPresent()) {
            classContext = classContext.getParent().orElseThrow(() ->
                    new IllegalStateException("Test class context is not available"));
        }
        return classContext.getStore(ExtensionContext.Namespace.create(NamespaceRegistry.class, testClass));
    }

    public ExtensionContext.Store methodStore(ExtensionContext context) {
        return context.getStore(ExtensionContext.Namespace.create(
                NamespaceRegistry.class, context.getRequiredTestClass(), context.getRequiredTestMethod()));
    }
}
