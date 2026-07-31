package io.bookwright.util;

import io.bookwright.api.ApiCallException;
import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;
import org.awaitility.core.ConditionTimeoutException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WaitsTest {

    @Test
    void retriesKnownTransientApiFailures() {
        AtomicInteger attempts = new AtomicInteger();

        Waits.await("transient API operation")
                .pollInterval(Duration.ofMillis(5))
                .atMost(Duration.ofSeconds(1))
                .until(() -> {
                    if (attempts.incrementAndGet() < 3) {
                        throw new ApiCallException("temporary transport failure", new IOException("reset"));
                    }
                    return true;
                });

        assertThat(attempts).hasValue(3);
    }

    @Test
    void programmingErrorsFailImmediately() {
        AtomicInteger attempts = new AtomicInteger();

        assertThatThrownBy(() -> Waits.await("invalid application state")
                .pollInterval(Duration.ofMillis(5))
                .atMost(Duration.ofSeconds(1))
                .until(() -> {
                    attempts.incrementAndGet();
                    throw new IllegalStateException("programming error");
                }))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("programming error");
        assertThat(attempts).hasValue(1);
    }

    @Test
    void timeoutDiagnosticsContainMandatoryAlias() {
        assertThatThrownBy(() -> Waits.await("booking appears in search")
                .pollInterval(Duration.ofMillis(5))
                .atMost(Duration.ofMillis(50))
                .until(() -> false))
                .isInstanceOf(ConditionTimeoutException.class)
                .hasMessageContaining("booking appears in search");
    }
}
