package io.bookwright.teardown;

import io.bookwright.junit.NamespaceRegistry;
import java.util.ArrayDeque;
import java.util.Deque;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * Method-scoped LIFO queue of cleanup actions. The instance lives in the JUnit Store, so ownership
 * follows the test lifecycle rather than a worker thread.
 */
public class TeardownStorage {

  private static final String STORE_KEY = "teardownStorage";

  public record TeardownAction(String name, Runnable action) {}

  private final Deque<TeardownAction> actions = new ArrayDeque<>();

  public static TeardownStorage getOrCreate(ExtensionContext context) {
    return NamespaceRegistry.methodStore(context)
        .getOrComputeIfAbsent(STORE_KEY, key -> new TeardownStorage(), TeardownStorage.class);
  }

  public static TeardownStorage get(ExtensionContext context) {
    return NamespaceRegistry.methodStore(context).get(STORE_KEY, TeardownStorage.class);
  }

  public void push(String name, Runnable action) {
    actions.addLast(new TeardownAction(name, action));
  }

  TeardownAction pollLast() {
    return actions.pollLast();
  }

  void clear() {
    actions.clear();
  }
}
