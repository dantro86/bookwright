package io.bookwright.steps;

import com.google.inject.Inject;
import lombok.Getter;
import lombok.experimental.Accessors;

/** The one object API tests receive. Small on purpose: add a field per domain, not forty-five. */
@Getter
@Accessors(fluent = true)
public class ApiSteps {
  @Inject private io.bookwright.steps.restfulbooker.RestfulBookerSteps restfulBooker;
  @Inject private io.bookwright.steps.local.LocalApiSteps local;
}
