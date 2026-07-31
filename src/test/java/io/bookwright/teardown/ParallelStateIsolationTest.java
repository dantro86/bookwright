package io.bookwright.teardown;

import io.bookwright.api.AuthSession;
import io.bookwright.api.model.Booking;
import io.bookwright.junit.TestSeeds;
import io.bookwright.util.TestData;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ParallelStateIsolationTest {

    private static final int WORKERS = 8;
    private static final long RUN_SEED = 7_777L;

    @Test
    void authDataAndTeardownRemainIsolatedUnderConcurrentLoad() throws Exception {
        CyclicBarrier barrier = new CyclicBarrier(WORKERS);
        List<Callable<WorkerResult>> tasks = new ArrayList<>();
        for (int worker = 0; worker < WORKERS; worker++) {
            int id = worker;
            tasks.add(() -> exerciseIsolatedState(id, barrier));
        }

        List<WorkerResult> results;
        try (var executor = Executors.newFixedThreadPool(WORKERS)) {
            results = executor.invokeAll(tasks).stream().map(future -> {
                try {
                    return future.get();
                } catch (Exception exception) {
                    throw new AssertionError("Parallel isolation worker failed", exception);
                }
            }).toList();
        }

        assertThat(results).extracting(WorkerResult::testSeed).doesNotHaveDuplicates();
        assertThat(results).extracting(result -> result.booking().getLastname()).doesNotHaveDuplicates();
        assertThat(results).extracting(WorkerResult::authCookie).doesNotHaveDuplicates();
        assertThat(results).allSatisfy(result -> assertThat(result.teardownOrder())
                .containsExactly("second-" + result.worker(), "first-" + result.worker()));
    }

    private WorkerResult exerciseIsolatedState(int worker, CyclicBarrier barrier) throws Exception {
        String testId = "parallel-worker-" + worker;
        long testSeed = TestSeeds.deriveTestSeed(RUN_SEED, testId);
        TestData data = new TestData(RUN_SEED, testSeed, testId);
        AuthSession session = new AuthSession("token-" + worker);
        TeardownStorage teardown = new TeardownStorage();
        List<String> cleanup = new ArrayList<>();
        teardown.push("first", () -> cleanup.add("first-" + worker));
        teardown.push("second", () -> cleanup.add("second-" + worker));
        Booking booking = data.booking();

        barrier.await(10, TimeUnit.SECONDS);
        TeardownExtension.execute(teardown, true, false);

        return new WorkerResult(worker, testSeed, booking, session.cookie(), List.copyOf(cleanup));
    }

    private record WorkerResult(
            int worker,
            long testSeed,
            Booking booking,
            String authCookie,
            List<String> teardownOrder) {
    }
}
