package io.bookwright.junit;

import io.bookwright.api.model.CreatedBooking;
import io.bookwright.steps.ApiSteps;
import io.bookwright.util.BookingFactory;
import java.util.function.BiConsumer;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * Catalog of test preconditions. Each constant pairs a human-readable title
 * with the setup action; results are shared with the test through the
 * method-scoped store (see {@link NamespaceRegistry} keys).
 */
public enum Precondition implements IPrecondition {

    AUTH_TOKEN("Obtain auth token",
            (api, store) -> store.put(NamespaceRegistry.AUTH_TOKEN_KEY, api.auth().token())),

    BOOKING_EXISTS("Create a booking",
            (api, store) -> {
                CreatedBooking created = api.bookings().create(BookingFactory.random());
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
