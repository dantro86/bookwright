package io.bookwright.junit;

import io.bookwright.api.model.CreatedBooking;
import io.bookwright.steps.ApiSteps;
import io.bookwright.util.TestData;
import java.util.function.BiConsumer;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * Catalog of test preconditions. Each constant pairs a human-readable title
 * with the setup action; results are shared with the test through the
 * method-scoped store (see {@link NamespaceRegistry} keys).
 */
public enum Precondition implements IPrecondition {

    AUTH_SESSION("Obtain auth session",
            (api, store) -> store.put(NamespaceRegistry.AUTH_SESSION_KEY, api.auth().session())),

    BOOKING_EXISTS("Create a booking",
            (api, store) -> {
                TestData data = store.get(NamespaceRegistry.TEST_DATA_KEY, TestData.class);
                if (data == null) {
                    throw new IllegalStateException("TestDataExtension did not initialize test data");
                }
                CreatedBooking created = api.bookings().create(data.booking());
                store.put(NamespaceRegistry.BOOKING_KEY, created);
            });

    private final String title;
    private final BiConsumer<ApiSteps, ExtensionContext.Store> action;

    Precondition(String title, BiConsumer<ApiSteps, ExtensionContext.Store> action) {
        this.title = title;
        this.action = action;
    }

    @Override
    public String title() {
        return title;
    }

    @Override
    public void execute(ApiSteps api, ExtensionContext.Store store) {
        action.accept(api, store);
    }
}
