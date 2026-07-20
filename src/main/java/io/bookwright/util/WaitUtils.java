package io.bookwright.util;

import java.time.Duration;
import java.util.function.Supplier;
import lombok.experimental.UtilityClass;
import org.awaitility.Awaitility;
import org.awaitility.core.ConditionTimeoutException;

/**
 * The single wait utility: Awaitility with sane defaults and an always-required
 * alias so timeout messages say what was actually awaited.
 */
@UtilityClass
public class WaitUtils {

    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(15);
    private static final Duration DEFAULT_POLL = Duration.ofMillis(500);

    public void waitForTrue(String alias, Supplier<Boolean> condition) {
        waitForTrue(alias, condition, DEFAULT_TIMEOUT, DEFAULT_POLL);
    }

    public void waitForTrue(String alias, Supplier<Boolean> condition, Duration timeout, Duration poll) {
        Awaitility.await(alias)
                .atMost(timeout)
                .pollInterval(poll)
                .ignoreExceptions()
                .until(condition::get);
    }

    public boolean waitForTrueNonFailing(String alias, Supplier<Boolean> condition, Duration timeout) {
        try {
            waitForTrue(alias, condition, timeout, DEFAULT_POLL);
            return true;
        } catch (ConditionTimeoutException e) {
            return false;
        }
    }

    public <T> T waitTillNotNull(String alias, Supplier<T> supplier) {
        return waitTillNotNull(alias, supplier, DEFAULT_TIMEOUT);
    }

    public <T> T waitTillNotNull(String alias, Supplier<T> supplier, Duration timeout) {
        return Awaitility.await(alias)
                .atMost(timeout)
                .pollInterval(DEFAULT_POLL)
                .ignoreExceptions()
                .until(supplier::get, java.util.Objects::nonNull);
    }
}
