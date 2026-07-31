package io.bookwright.junit;

import com.microsoft.playwright.Page;
import io.bookwright.ui.BrowserManager;
import io.qameta.allure.Allure;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

/** Attaches rich Playwright diagnostics while the failed test's browser context is still alive. */
@Slf4j
public class UiArtifactsOnFailureExtension implements AfterTestExecutionCallback {

  @Override
  public void afterTestExecution(ExtensionContext context) {
    if (context.getExecutionException().isEmpty()) {
      return;
    }
    Page page = BrowserManager.activePageOrNull();
    if (page == null) {
      return;
    }

    captureAll(
        new Artifact("screenshot", () -> attachScreenshot(page)),
        new Artifact("page HTML", () -> attachHtml(page)),
        new Artifact("browser diagnostics", UiArtifactsOnFailureExtension::attachDiagnostics),
        new Artifact("Playwright trace", UiArtifactsOnFailureExtension::attachTrace));
  }

  private static void attachScreenshot(Page page) {
    byte[] screenshot = page.screenshot(new Page.ScreenshotOptions().setFullPage(true));
    Allure.addAttachment(
        "Screenshot on failure", "image/png", new ByteArrayInputStream(screenshot), ".png");
  }

  private static void attachHtml(Page page) {
    Allure.addAttachment(
        "Page HTML on failure",
        "text/html",
        new ByteArrayInputStream(page.content().getBytes(StandardCharsets.UTF_8)),
        ".html");
  }

  private static void attachDiagnostics() {
    Allure.addAttachment(
        "Browser diagnostics on failure", "text/plain", BrowserManager.diagnosticsReport(), ".txt");
  }

  private static void attachTrace() throws IOException {
    Path trace = Files.createTempFile("bookwright-playwright-trace-", ".zip");
    try {
      BrowserManager.saveTrace(trace);
      try (InputStream traceContent = Files.newInputStream(trace)) {
        Allure.addAttachment(
            "Playwright trace on failure", "application/zip", traceContent, ".zip");
      }
    } finally {
      Files.deleteIfExists(trace);
    }
  }

  static void captureAll(Artifact... artifacts) {
    for (Artifact artifact : artifacts) {
      try {
        artifact.capture().run();
      } catch (Exception e) {
        log.warn("Could not capture {}: {}", artifact.name(), e.getMessage());
      }
    }
  }

  @FunctionalInterface
  interface ThrowingRunnable {
    void run() throws Exception;
  }

  record Artifact(String name, ThrowingRunnable capture) {}
}
