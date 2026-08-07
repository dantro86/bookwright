package io.bookwright.tests.framework;

import static org.assertj.core.api.Assertions.assertThat;

import io.bookwright.steps.ApiSteps;
import io.bookwright.steps.restfulbooker.auth.AuthSteps;
import io.bookwright.util.TestData;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

class StoreIsolationTest {

  private static final Set<ApiSteps> API_FACADES = ConcurrentHashMap.newKeySet();
  private static final Set<AuthSteps> AUTH_STEPS = ConcurrentHashMap.newKeySet();
  private static final Set<Long> TEST_SEEDS = ConcurrentHashMap.newKeySet();

  @Test
  void firstMethodOwnsItsStore(TestData data, ApiSteps api) {
    recordIsolation(data, api);
  }

  @Test
  void secondMethodOwnsItsStore(TestData data, ApiSteps api) {
    recordIsolation(data, api);
  }

  @AfterAll
  static void facadesAreMethodScoped() {
    assertThat(API_FACADES).hasSize(2);
    assertThat(AUTH_STEPS).hasSize(2);
    assertThat(TEST_SEEDS).hasSize(2);
  }

  private void recordIsolation(TestData data, ApiSteps api) {
    API_FACADES.add(api);
    AUTH_STEPS.add(api.restfulBooker().auth());
    TEST_SEEDS.add(data.testSeed());
  }
}
