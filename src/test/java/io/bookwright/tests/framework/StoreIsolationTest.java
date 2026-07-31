package io.bookwright.tests.framework;

import io.bookwright.junit.TestStore;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StoreIsolationTest {

    private static final String KEY = "method-isolation-probe";

    @Test
    void firstMethodOwnsItsStore(TestStore store) {
        assertIsolated(store, "first");
    }

    @Test
    void secondMethodOwnsItsStore(TestStore store) {
        assertIsolated(store, "second");
    }

    private void assertIsolated(TestStore store, String value) {
        assertThatThrownBy(() -> store.get(KEY, String.class))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Did you forget the fixture/precondition");

        store.put(KEY, value);

        assertThat(store.get(KEY, String.class)).isEqualTo(value);
    }
}
