package io.bookwright.ui;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UiDiagnosticsTest {

    @Test
    void reportsPageContextAndCollectedFailures() {
        UiDiagnostics diagnostics = new UiDiagnostics();
        diagnostics.recordConsoleError("ReferenceError: app is not defined", "app.js:42");
        diagnostics.recordPageError("Unhandled promise rejection");
        diagnostics.recordFailedRequest(
                "GET",
                "https://example.test/profile?token=browser-secret&view=compact",
                "xhr",
                "net::ERR_FAILED");

        String report = diagnostics.report(
                "https://example.test/inventory?access_token=page-secret",
                1920,
                1080);

        assertThat(report)
                .contains(
                        "URL: https://example.test/inventory?access_token=%5BREDACTED%5D",
                        "Viewport: 1920x1080",
                        "Console errors (1)",
                        "ReferenceError: app is not defined at app.js:42",
                        "Page errors (1)",
                        "Unhandled promise rejection",
                        "Failed requests (1)",
                        "view=compact&token=%5BREDACTED%5D",
                        "net::ERR_FAILED")
                .doesNotContain("browser-secret", "page-secret");
    }

    @Test
    void reportsEmptyCategoriesExplicitly() {
        String report = new UiDiagnostics().report("about:blank", 1280, 720);

        assertThat(report)
                .contains("Viewport: 1280x720")
                .contains("Console errors (0):\n<none>")
                .contains("Page errors (0):\n<none>")
                .contains("Failed requests (0):\n<none>");
    }
}
