package io.bookwright.steps;

import com.google.inject.Inject;
import lombok.Getter;
import lombok.experimental.Accessors;

/**
 * The one object API tests receive. Small on purpose: add a field per domain,
 * not forty-five.
 */
@Getter
@Accessors(fluent = true)
public class ApiSteps {

    @Inject
    private AuthApiSteps auth;

    @Inject
    private BookingApiSteps bookings;
}
