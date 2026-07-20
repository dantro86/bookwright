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
 * after each UI test; the browser itself dies with the JVM.
 */
@Slf4j
public final class BrowserManager {

    private record Session(Playwright playwright, Browser browser) {
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
        if (SESSION.get() == null) {
            Playwright playwright = Playwright.create();
            Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
                    .setHeadless(Configs.main().uiHeadless()));
            SESSION.set(new Session(playwright, browser));
        }
        return SESSION.get().browser();
    }
}
