package io.bookwright.ui;

import io.bookwright.api.SecretSanitizer;
import java.util.ArrayList;
import java.util.List;

/** Collects bounded, per-test browser diagnostics for failure reporting. */
final class UiDiagnostics {

  private static final int MAX_ENTRIES_PER_CATEGORY = 100;
  private static final int MAX_ENTRY_LENGTH = 2_000;

  private final List<String> consoleErrors = new ArrayList<>();
  private final List<String> pageErrors = new ArrayList<>();
  private final List<String> failedRequests = new ArrayList<>();

  synchronized void recordConsoleError(String text, String location) {
    add(consoleErrors, "%s%s".formatted(text, suffix(" at ", location)));
  }

  synchronized void recordPageError(String error) {
    add(pageErrors, error);
  }

  synchronized void recordFailedRequest(
      String method, String url, String resourceType, String failure) {
    add(
        failedRequests,
        "%s %s [%s] — %s".formatted(method, SecretSanitizer.url(url), resourceType, failure));
  }

  synchronized String report(String pageUrl, int viewportWidth, int viewportHeight) {
    return """
                URL: %s
                Viewport: %dx%d

                Console errors (%d):
                %s

                Page errors (%d):
                %s

                Failed requests (%d):
                %s
                """
        .formatted(
            SecretSanitizer.url(pageUrl),
            viewportWidth,
            viewportHeight,
            consoleErrors.size(),
            entries(consoleErrors),
            pageErrors.size(),
            entries(pageErrors),
            failedRequests.size(),
            entries(failedRequests));
  }

  private static void add(List<String> entries, String value) {
    if (entries.size() >= MAX_ENTRIES_PER_CATEGORY) {
      return;
    }
    String safeValue = value == null || value.isBlank() ? "<no details>" : value;
    entries.add(safeValue.substring(0, Math.min(safeValue.length(), MAX_ENTRY_LENGTH)));
  }

  private static String entries(List<String> entries) {
    if (entries.isEmpty()) {
      return "<none>";
    }
    StringBuilder result = new StringBuilder();
    for (int index = 0; index < entries.size(); index++) {
      result.append(index + 1).append(". ").append(entries.get(index));
      if (index < entries.size() - 1) {
        result.append(System.lineSeparator());
      }
    }
    return result.toString();
  }

  private static String suffix(String prefix, String value) {
    return value == null || value.isBlank() ? "" : prefix + value;
  }
}
