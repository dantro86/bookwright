package io.bookwright.steps.local;

import com.google.inject.Inject;
import io.bookwright.steps.local.auth.AuthSteps;
import io.bookwright.steps.local.bookings.BookingSteps;
import io.bookwright.steps.local.users.UserSteps;
import lombok.Getter;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
public class LocalApiSteps {
  @Inject private AuthSteps auth;
  @Inject private UserSteps users;
  @Inject private BookingSteps bookings;
}
