package io.bookwright.ui;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BrowserManagerTest {

    @Test
    void closesBrowserBeforePlaywright() {
        List<String> order = new ArrayList<>();

        BrowserManager.closeInOrder(
                () -> order.add("browser"),
                () -> order.add("playwright"));

        assertThat(order).containsExactly("browser", "playwright");
    }

    @Test
    void closesPlaywrightEvenWhenBrowserCloseFails() {
        List<String> order = new ArrayList<>();

        assertThatThrownBy(() -> BrowserManager.closeInOrder(
                () -> {
                    order.add("browser");
                    throw new IllegalStateException("browser close failed");
                },
                () -> order.add("playwright")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("browser close failed");
        assertThat(order).containsExactly("browser", "playwright");
    }
}
