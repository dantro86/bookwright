package io.bookwright.teardown;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TeardownExtensionTest {

    @Test
    void executesActionsInReverseOrder() {
        TeardownStorage storage = new TeardownStorage();
        List<String> executionOrder = new ArrayList<>();
        storage.push("first", () -> executionOrder.add("first"));
        storage.push("second", () -> executionOrder.add("second"));

        TeardownExtension.execute(storage, true, false);

        assertThat(executionOrder).containsExactly("second", "first");
    }

    @Test
    void failsSuccessfulTestWhenConfigured() {
        TeardownStorage storage = failingStorage();

        assertThatThrownBy(() -> TeardownExtension.execute(storage, true, false))
                .isInstanceOf(TeardownException.class)
                .hasMessage("1 teardown action(s) failed")
                .satisfies(error -> assertThat(error.getSuppressed()).hasSize(1));
    }

    @Test
    void preservesPrimaryTestFailure() {
        assertThatCode(() -> TeardownExtension.execute(failingStorage(), true, true))
                .doesNotThrowAnyException();
    }

    @Test
    void canLogCleanupFailureWithoutFailingTest() {
        assertThatCode(() -> TeardownExtension.execute(failingStorage(), false, false))
                .doesNotThrowAnyException();
    }

    private TeardownStorage failingStorage() {
        TeardownStorage storage = new TeardownStorage();
        storage.push("broken cleanup", () -> {
            throw new IllegalStateException("cleanup failed");
        });
        return storage;
    }
}
