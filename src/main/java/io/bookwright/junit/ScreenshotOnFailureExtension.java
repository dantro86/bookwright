package io.bookwright.junit;

import com.microsoft.playwright.Page;
import io.bookwright.ui.BrowserManager;
import io.qameta.allure.Allure;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * Auto-registered callback: when a test fails and there is a live Playwright page
 * on this thread, attaches a screenshot and the page HTML to the Allure report.
 * Must run as {@link AfterTestExecutionCallback} (not TestWatcher): at this point
 * the browser context is still open and the Allure test case is still active.
 * API tests have no page and are unaffected.
 */
@Slf4j
public class ScreenshotOnFailureExtension implements AfterTestExecutionCallback {

    @Override
    public void afterTestExecution(ExtensionContext context) {
        if (context.getExecutionException().isEmpty()) {
            return;
        }
        Page page = BrowserManager.activePageOrNull();
        if (page == null) {
            return;
        }
        try {
            byte[] screenshot = page.screenshot(new Page.ScreenshotOptions().setFullPage(true));
            Allure.addAttachment("Screenshot on failure", "image/png",
                    new ByteArrayInputStream(screenshot), ".png");
            Allure.addAttachment("Page HTML on failure", "text/html",
                    new ByteArrayInputStream(page.content().getBytes(StandardCharsets.UTF_8)), ".html");
        } catch (RuntimeException e) {
            log.warn("Could not capture failure artifacts: {}", e.getMessage());
        }
    }
}
