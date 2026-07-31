package io.bookwright.teardown;

import io.bookwright.config.Configs;
import io.qameta.allure.Allure;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * Auto-registered (META-INF/services) cleanup runner: drains the LIFO teardown
 * queue after every test. Cleanup failures never replace a primary test failure;
 * after a successful test they follow the configured fail-on-error policy.
 */
@Slf4j
public class TeardownExtension implements AfterEachCallback {

    @Override
    public void afterEach(ExtensionContext context) {
        TeardownStorage storage = TeardownStorage.get(context);
        if (storage == null) {
            return;
        }
        execute(storage, Configs.main().teardownFailOnError(), context.getExecutionException().isPresent());
    }

    static void execute(TeardownStorage storage, boolean failOnError, boolean primaryTestFailed) {
        List<Exception> failures = new ArrayList<>();
        TeardownStorage.TeardownAction action;
        try {
            while ((action = storage.pollLast()) != null) {
                TeardownStorage.TeardownAction current = action;
                try {
                    Allure.step("Teardown: " + current.name(), () -> current.action().run());
                } catch (Exception e) {
                    failures.add(e);
                    log.warn("Teardown '{}' failed: {}", current.name(), e.getMessage(), e);
                }
            }
        } finally {
            storage.clear();
        }

        if (failures.isEmpty()) {
            return;
        }

        Allure.addAttachment("Teardown failures", failures.stream()
                .map(Throwable::toString)
                .reduce((left, right) -> left + System.lineSeparator() + right)
                .orElse(""));

        if (failOnError && !primaryTestFailed) {
            TeardownException error = new TeardownException(
                    "%d teardown action(s) failed".formatted(failures.size()));
            failures.forEach(error::addSuppressed);
            throw error;
        }
    }
}
