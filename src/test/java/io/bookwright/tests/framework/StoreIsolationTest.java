package io.bookwright.tests.framework;

import io.bookwright.junit.TestStore;
import io.bookwright.steps.ApiSteps;
import io.bookwright.steps.AuthApiSteps;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Execution(ExecutionMode.CONCURRENT)
class StoreIsolationTest {

    private static final String KEY = "method-isolation-probe";
    private static final CyclicBarrier BARRIER = new CyclicBarrier(2);
    private static final Set<ApiSteps> API_FACADES = ConcurrentHashMap.newKeySet();
    private static final Set<AuthApiSteps> AUTH_STEPS = ConcurrentHashMap.newKeySet();

    @Test
    void firstMethodOwnsItsStore(TestStore store, ApiSteps api) throws Exception {
        assertIsolated(store, api, "first");
    }

    @Test
    void secondMethodOwnsItsStore(TestStore store, ApiSteps api) throws Exception {
        assertIsolated(store, api, "second");
    }

    private void assertIsolated(TestStore store, ApiSteps api, String value) throws Exception {
        assertThatThrownBy(() -> store.get(KEY, String.class))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Did you forget the fixture/precondition");

        store.put(KEY, value);
        API_FACADES.add(api);
        AUTH_STEPS.add(api.auth());
        BARRIER.await(10, TimeUnit.SECONDS);

        assertThat(store.get(KEY, String.class)).isEqualTo(value);
        assertThat(API_FACADES).hasSize(2);
        assertThat(AUTH_STEPS).hasSize(2);
    }
}
