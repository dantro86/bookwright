package io.bookwright.tests.framework;

import com.microsoft.playwright.Page;
import io.bookwright.steps.UiSteps;
import io.bookwright.ui.BrowserManager;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import static org.assertj.core.api.Assertions.assertThat;

@Execution(ExecutionMode.CONCURRENT)
class BrowserContextIsolationTest {

    private static final CyclicBarrier BARRIER = new CyclicBarrier(2);

    @Test
    void firstConcurrentTestOwnsItsPage(UiSteps ignored) throws Exception {
        assertIsolatedPage("first");
    }

    @Test
    void secondConcurrentTestOwnsItsPage(UiSteps ignored) throws Exception {
        assertIsolatedPage("second");
    }

    private void assertIsolatedPage(String marker) throws Exception {
        Page page = BrowserManager.activePageOrNull();
        assertThat(page).as("active page provided with UiSteps").isNotNull();
        page.setContent("<main data-marker='%s'>%s</main>".formatted(marker, marker));
        page.evaluate("marker => window.__bookwrightMarker = marker", marker);

        BARRIER.await(10, TimeUnit.SECONDS);

        assertThat(page.locator("main").getAttribute("data-marker")).isEqualTo(marker);
        assertThat(page.evaluate("() => window.__bookwrightMarker")).isEqualTo(marker);
    }
}
