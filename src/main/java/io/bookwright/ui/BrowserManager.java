package io.bookwright.ui;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.Tracing;
import com.microsoft.playwright.options.ViewportSize;
import io.bookwright.config.Configs;
import java.nio.file.Path;
import lombok.extern.slf4j.Slf4j;

/**
 * Per-thread Playwright lifecycle. One Playwright+Browser per worker thread
 * (expensive, reused between tests); one fresh BrowserContext+Page per test
 * (cheap, gives isolation). {@code closeContext()} is called by the resolver
 * after each UI test; the owning class store closes Browser and then Playwright.
 */
@Slf4j
public final class BrowserManager {

    private static final class Session implements AutoCloseable {
        private final Playwright playwright;
        private final Browser browser;
        private boolean closed;

        private Session(Playwright playwright, Browser browser) {
            this.playwright = playwright;
            this.browser = browser;
        }

        private Browser browser() {
            return browser;
        }

        private boolean isClosed() {
            return closed;
        }

        @Override
        public void close() {
            if (closed) {
                return;
            }
            try {
                closeInOrder(browser::close, playwright::close);
            } finally {
                closed = true;
                log.info("Playwright browser session closed");
            }
        }
    }

    private static final class TestContext {
        private final BrowserContext context;
        private final Page page;
        private final UiDiagnostics diagnostics;
        private boolean tracing;

        private TestContext(BrowserContext context, Page page, UiDiagnostics diagnostics) {
            this.context = context;
            this.page = page;
            this.diagnostics = diagnostics;
            this.tracing = true;
        }

        private void stopTrace(Path path) {
            if (!tracing) {
                return;
            }
            context.tracing().stop(new Tracing.StopOptions().setPath(path));
            tracing = false;
        }

        private void discardTrace() {
            if (!tracing) {
                return;
            }
            context.tracing().stop();
            tracing = false;
        }
    }

    private static final ThreadLocal<Session> SESSION = new ThreadLocal<>();
    private static final ThreadLocal<TestContext> TEST_CONTEXT = new ThreadLocal<>();

    private BrowserManager() {
    }

    static void closeInOrder(Runnable browserClose, Runnable playwrightClose) {
        try {
            browserClose.run();
        } finally {
            playwrightClose.run();
        }
    }

    public static Page page() {
        if (TEST_CONTEXT.get() == null) {
            BrowserContext context = browser().newContext(new Browser.NewContextOptions()
                    .setViewportSize(1920, 1080));
            context.tracing().start(new Tracing.StartOptions()
                    .setScreenshots(true)
                    .setSnapshots(true)
                    .setSources(true));
            Page page = context.newPage();
            UiDiagnostics diagnostics = new UiDiagnostics();
            bindDiagnostics(page, diagnostics);
            TEST_CONTEXT.set(new TestContext(context, page, diagnostics));
        }
        return TEST_CONTEXT.get().page;
    }

    /** The page of the currently running test on this thread, or null if none. */
    public static Page activePageOrNull() {
        TestContext current = TEST_CONTEXT.get();
        return current == null ? null : current.page;
    }

    public static String diagnosticsReport() {
        TestContext current = requiredTestContext();
        ViewportSize viewport = current.page.viewportSize();
        int width = viewport == null ? 0 : viewport.width;
        int height = viewport == null ? 0 : viewport.height;
        return current.diagnostics.report(current.page.url(), width, height);
    }

    public static void saveTrace(Path path) {
        requiredTestContext().stopTrace(path);
    }

    /** Captures the current thread's browser session for class-store cleanup. */
    public static AutoCloseable sessionResource() {
        Session captured = session();
        return captured::close;
    }

    public static void closeContext() {
        TestContext current = TEST_CONTEXT.get();
        if (current != null) {
            try {
                current.discardTrace();
            } catch (RuntimeException e) {
                log.warn("Failed to stop Playwright trace: {}", e.getMessage());
            }
            try {
                current.context.close();
            } catch (RuntimeException e) {
                log.warn("Failed to close browser context: {}", e.getMessage());
            }
        }
        TEST_CONTEXT.remove();
    }

    private static Browser browser() {
        return session().browser();
    }

    private static TestContext requiredTestContext() {
        TestContext current = TEST_CONTEXT.get();
        if (current == null) {
            throw new IllegalStateException("No active Playwright test context");
        }
        return current;
    }

    private static void bindDiagnostics(Page page, UiDiagnostics diagnostics) {
        page.onConsoleMessage(message -> {
            if ("error".equalsIgnoreCase(message.type())) {
                diagnostics.recordConsoleError(message.text(), message.location());
            }
        });
        page.onPageError(diagnostics::recordPageError);
        page.onRequestFailed(request -> diagnostics.recordFailedRequest(
                request.method(), request.url(), request.resourceType(), request.failure()));
    }

    private static Session session() {
        Session current = SESSION.get();
        if (current == null || current.isClosed()) {
            Playwright playwright = Playwright.create();
            Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
                    .setHeadless(Configs.main().uiHeadless()));
            current = new Session(playwright, browser);
            SESSION.set(current);
        }
        return current;
    }
}
