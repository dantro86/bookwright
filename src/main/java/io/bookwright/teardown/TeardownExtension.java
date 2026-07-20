package io.bookwright.teardown;

import io.qameta.allure.Allure;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * Auto-registered (META-INF/services) cleanup runner: drains the LIFO teardown
 * queue after every test. Failures are logged, never rethrown — a broken cleanup
 * must not repaint a passed test.
 */
@Slf4j
public class TeardownExtension implements AfterEachCallback {

    @Override
    public void afterEach(ExtensionContext context) {
        TeardownStorage storage = new TeardownStorage();
        TeardownStorage.TeardownAction action;
        while ((action = storage.pollLast()) != null) {
            TeardownStorage.TeardownAction current = action;
            try {
                Allure.step("Teardown: " + current.name(), () -> current.action().run());
            } catch (Exception e) {
                log.warn("Teardown '{}' failed: {}", current.name(), e.getMessage(), e);
            }
        }
        TeardownStorage.clear();
    }
}
