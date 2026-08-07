package io.bookwright.junit;

import io.bookwright.api.model.CreatedBooking;
import io.bookwright.steps.ApiSteps;
import io.bookwright.util.TestData;
import java.util.function.BiConsumer;

/**
 * Catalog of test preconditions. Each constant pairs a human-readable title with the setup action;
 * results are shared with the test through typed accessors on the method-scoped {@link TestStore}.
 */
public enum Precondition implements IPrecondition {
  BOOKING_EXISTS(
      "Create a booking",
      (api, store) -> {
        TestData data = store.testData();
        CreatedBooking created =
            api.restfulBooker()
                .bookings()
                .create(data.booking(), api.restfulBooker().auth().session());
        store.putBooking(created);
      });

  static final String BOOKING_KEY = "createdBooking";

  private final String title;
  private final BiConsumer<ApiSteps, TestStore> action;

  Precondition(String title, BiConsumer<ApiSteps, TestStore> action) {
    this.title = title;
    this.action = action;
  }

  @Override
  public String title() {
    return title;
  }

  @Override
  public void execute(ApiSteps api, TestStore store) {
    action.accept(api, store);
  }
}
