package io.bookwright.tests.framework;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.bookwright.junit.TestStore;
import io.bookwright.steps.ApiSteps;
import io.bookwright.steps.restfulbooker.auth.AuthSteps;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

class StoreIsolationTest {

  private static final String KEY = "method-isolation-probe";
  private static final Set<ApiSteps> API_FACADES = ConcurrentHashMap.newKeySet();
  private static final Set<AuthSteps> AUTH_STEPS = ConcurrentHashMap.newKeySet();

  @Test
  void firstMethodOwnsItsStore(TestStore store, ApiSteps api) {
    assertIsolated(store, api, "first");
  }

  @Test
  void secondMethodOwnsItsStore(TestStore store, ApiSteps api) {
    assertIsolated(store, api, "second");
  }

  @AfterAll
  static void facadesAreMethodScoped() {
    assertThat(API_FACADES).hasSize(2);
    assertThat(AUTH_STEPS).hasSize(2);
  }

  private void assertIsolated(TestStore store, ApiSteps api, String value) {
    assertThatThrownBy(() -> store.get(KEY, String.class))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("Did you forget the fixture/precondition");

    store.put(KEY, value);
    API_FACADES.add(api);
    AUTH_STEPS.add(api.restfulBooker().auth());

    assertThat(store.get(KEY, String.class)).isEqualTo(value);
  }
}
