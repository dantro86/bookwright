package io.bookwright.steps.ui.local;

import com.google.inject.Inject;
import io.bookwright.steps.ui.local.bookings.BookingSteps;
import lombok.Getter;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
public class LocalUiSteps {
  @Inject private BookingSteps bookings;
}
