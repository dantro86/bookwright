package io.bookwright.util;

import java.time.Duration;
import lombok.experimental.UtilityClass;
import org.awaitility.Awaitility;
import org.awaitility.core.ConditionFactory;

/**
 * Preconfigured Awaitility entry points. Deliberately tiny: this is not a wait
 * "framework", just shared defaults + a mandatory alias so every timeout message
 * says what was actually awaited. Compose the rest fluently at the call site:
 *
 * <pre>{@code
 * Waits.await("booking %d in search".formatted(id))
 *      .until(() -> search(id).contains(id));
 * }</pre>
 */
@UtilityClass
public class Waits {

    /** Default preset: 15s timeout, 500ms poll, exceptions during polling ignored. */
    public ConditionFactory await(String alias) {
        return Awaitility.await(alias)
                .atMost(Duration.ofSeconds(15))
                .pollInterval(Duration.ofMillis(500))
                .ignoreExceptions();
    }

    /** Slow preset for infrastructure warm-up (docker stand, cold heroku dyno): 90s / 3s. */
    public ConditionFactory awaitSlow(String alias) {
        return Awaitility.await(alias)
                .atMost(Duration.ofSeconds(90))
                .pollInterval(Duration.ofSeconds(3))
                .ignoreExceptions();
    }
}
