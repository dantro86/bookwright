package io.bookwright.steps.restfulbooker;

import com.google.inject.Inject;
import io.bookwright.steps.restfulbooker.auth.AuthSteps;
import io.bookwright.steps.restfulbooker.bookings.BookingSteps;
import io.bookwright.steps.restfulbooker.health.HealthSteps;
import lombok.Getter;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
public class RestfulBookerSteps {
  @Inject private AuthSteps auth;
  @Inject private HealthSteps health;
  @Inject private BookingSteps bookings;
}
