package io.bookwright.junit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.bookwright.steps.ApiSteps;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;

class PreconditionProviderTest {

  @Test
  void executesPreconditionsInDeclarationOrderAndSharesStoreData() {
    List<String> order = new ArrayList<>();
    TestStore store = new TestStore(inMemoryStore());
    IPrecondition first =
        precondition(
            "first",
            (api, state) -> {
              order.add("first");
              state.put("fixture", "ready");
            });
    IPrecondition second =
        precondition(
            "second",
            (api, state) -> {
              order.add("second");
              assertThat(state.getRequired("fixture", String.class)).isEqualTo("ready");
            });

    PreconditionProvider.execute(new IPrecondition[] {first, second}, new ApiSteps(), store);

    assertThat(order).containsExactly("first", "second");
  }

  @Test
  void stopsWhenAPreconditionFails() {
    List<String> order = new ArrayList<>();
    IPrecondition failing =
        precondition(
            "failing",
            (api, store) -> {
              order.add("failing");
              throw new IllegalStateException("fixture failed");
            });
    IPrecondition unreachable =
        precondition("unreachable", (api, store) -> order.add("unreachable"));

    assertThatThrownBy(
            () ->
                PreconditionProvider.execute(
                    new IPrecondition[] {failing, unreachable},
                    new ApiSteps(),
                    new TestStore(inMemoryStore())))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("fixture failed");
    assertThat(order).containsExactly("failing");
  }

  private IPrecondition precondition(String title, PreconditionAction action) {
    return new IPrecondition() {
      @Override
      public String title() {
        return title;
      }

      @Override
      public void execute(ApiSteps api, TestStore store) {
        action.execute(api, store);
      }
    };
  }

  private ExtensionContext.Store inMemoryStore() {
    Map<Object, Object> values = new HashMap<>();
    return (ExtensionContext.Store)
        Proxy.newProxyInstance(
            getClass().getClassLoader(),
            new Class<?>[] {ExtensionContext.Store.class},
            (proxy, method, arguments) ->
                switch (method.getName()) {
                  case "put" -> {
                    values.put(arguments[0], arguments[1]);
                    yield null;
                  }
                  case "get" -> values.get(arguments[0]);
                  case "remove" -> values.remove(arguments[0]);
                  default -> throw new UnsupportedOperationException(method.getName());
                });
  }

  @FunctionalInterface
  private interface PreconditionAction {
    void execute(ApiSteps api, TestStore store);
  }
}
