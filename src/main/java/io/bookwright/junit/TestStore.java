package io.bookwright.junit;

import io.bookwright.api.AuthSession;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * Thin typed wrapper over the method-scoped JUnit store. Injected into tests so they can read data
 * produced by fixtures and preconditions.
 */
public class TestStore {

  private final ExtensionContext.Store store;

  TestStore(ExtensionContext context) {
    this.store = NamespaceRegistry.methodStore(context);
  }

  public <T> T get(String key, Class<T> type) {
    T value = store.get(key, type);
    if (value == null) {
      throw new IllegalStateException(
          "No '%s' in the test store. Did you forget the fixture/precondition that provides it?"
              .formatted(key));
    }
    return value;
  }

  public void put(String key, Object value) {
    store.put(key, value);
  }

  public AuthSession authSession() {
    return get(NamespaceRegistry.AUTH_SESSION_KEY, AuthSession.class);
  }

  public TestUser testUser() {
    return get(NamespaceRegistry.TEST_USER_KEY, TestUser.class);
  }
}
