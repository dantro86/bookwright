package io.bookwright.junit;

import io.bookwright.api.model.Booking;
import io.bookwright.util.TestData;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TestSeedsTest {

    private static final long RUN_SEED = 4_242L;

    @Test
    void sameSeedAndIdentityProduceSameSequence() {
        TestData first = data(RUN_SEED, "bookingCanBeCreated");
        TestData replay = data(RUN_SEED, "bookingCanBeCreated");

        assertThat(List.of(first.booking(), first.booking(), first.booking()))
                .isEqualTo(List.of(replay.booking(), replay.booking(), replay.booking()));
    }

    @Test
    void testIdentityIsolatesRandomSequences() {
        TestData first = data(RUN_SEED, "firstTest");
        TestData second = data(RUN_SEED, "secondTest");

        assertThat(first.testSeed()).isNotEqualTo(second.testSeed());
        assertThat(first.booking()).isNotEqualTo(second.booking());
    }

    @Test
    void runSeedChangesGeneratedData() {
        TestData first = data(RUN_SEED, "sameTest");
        TestData second = data(RUN_SEED + 1, "sameTest");

        assertThat(first.booking()).isNotEqualTo(second.booking());
    }

    @Test
    void parallelSchedulingDoesNotChangePerTestData() throws Exception {
        List<String> testIds = List.of("test-a", "test-b", "test-c", "test-d");
        List<Booking> expected = testIds.stream().map(id -> data(RUN_SEED, id).booking()).toList();

        try (var executor = Executors.newFixedThreadPool(testIds.size())) {
            List<Callable<Booking>> tasks = new ArrayList<>();
            for (String testId : testIds) {
                tasks.add(() -> data(RUN_SEED, testId).booking());
            }
            List<Booking> actual = executor.invokeAll(tasks).stream().map(future -> {
                try {
                    return future.get();
                } catch (Exception exception) {
                    throw new AssertionError("Could not generate parallel test data", exception);
                }
            }).toList();

            assertThat(actual).isEqualTo(expected);
        }
    }

    @Test
    void invalidConfiguredSeedHasActionableMessage() {
        assertThatThrownBy(() -> TestSeeds.parse("not-a-number"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("signed 64-bit integer")
                .hasMessageContaining("not-a-number");
    }

    private TestData data(long runSeed, String testId) {
        return new TestData(runSeed, TestSeeds.deriveTestSeed(runSeed, testId), testId);
    }
}
