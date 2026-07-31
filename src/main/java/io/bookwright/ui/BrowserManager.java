package io.bookwright.ui;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import io.bookwright.config.Configs;
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
                browser.close();
            } finally {
                playwright.close();
                closed = true;
                log.info("Playwright browser session closed");
            }
        }
    }

    private static final ThreadLocal<Session> SESSION = new ThreadLocal<>();
    private static final ThreadLocal<BrowserContext> CONTEXT = new ThreadLocal<>();
    private static final ThreadLocal<Page> PAGE = new ThreadLocal<>();

    private BrowserManager() {
    }

    public static Page page() {
        if (PAGE.get() == null) {
            BrowserContext context = browser().newContext(new Browser.NewContextOptions()
                    .setViewportSize(1920, 1080));
            CONTEXT.set(context);
            PAGE.set(context.newPage());
        }
        return PAGE.get();
    }

    /** The page of the currently running test on this thread, or null if none. */
    public static Page activePageOrNull() {
        return PAGE.get();
    }

    /** Captures the current thread's browser session for class-store cleanup. */
    public static AutoCloseable sessionResource() {
        Session captured = session();
        return captured::close;
    }

    public static void closeContext() {
        BrowserContext context = CONTEXT.get();
        if (context != null) {
            try {
                context.close();
            } catch (RuntimeException e) {
                log.warn("Failed to close browser context: {}", e.getMessage());
            }
        }
        CONTEXT.remove();
        PAGE.remove();
    }

    private static Browser browser() {
        return session().browser();
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
