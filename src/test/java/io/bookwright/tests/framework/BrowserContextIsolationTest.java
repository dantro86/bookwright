package io.bookwright.tests.framework;

import static org.assertj.core.api.Assertions.assertThat;

import com.microsoft.playwright.Page;
import io.bookwright.ui.BrowserManager;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

class BrowserContextIsolationTest {

  private static final int WORKERS = 2;

  @Test
  void concurrentWorkersOwnIndependentPagesAndSessions() throws Exception {
    CyclicBarrier barrier = new CyclicBarrier(WORKERS);
    List<Callable<String>> tasks = new ArrayList<>();
    tasks.add(() -> exercisePage("first", barrier));
    tasks.add(() -> exercisePage("second", barrier));

    List<String> markers;
    try (var executor = Executors.newFixedThreadPool(WORKERS)) {
      markers =
          executor.invokeAll(tasks).stream()
              .map(
                  future -> {
                    try {
                      return future.get();
                    } catch (Exception exception) {
                      throw new AssertionError("Concurrent browser worker failed", exception);
                    }
                  })
              .toList();
    }

    assertThat(markers).containsExactlyInAnyOrder("first", "second");
  }

  private String exercisePage(String marker, CyclicBarrier barrier) throws Exception {
    Page page = BrowserManager.page();
    AutoCloseable session = BrowserManager.sessionResource();
    try {
      page.setContent("<main data-marker='%s'>%s</main>".formatted(marker, marker));
      page.evaluate("value => window.__bookwrightMarker = value", marker);
      barrier.await(30, TimeUnit.SECONDS);

      assertThat(page.locator("main").getAttribute("data-marker")).isEqualTo(marker);
      return (String) page.evaluate("() => window.__bookwrightMarker");
    } finally {
      BrowserManager.closeContext();
      session.close();
    }
  }
}
