package io.bookwright.steps.restfulbooker.health;

import com.google.inject.Inject;
import io.bookwright.api.restfulbooker.health.HealthApi;
import io.bookwright.util.Calls;
import io.bookwright.util.Waits;
import io.qameta.allure.Step;

public class HealthSteps {

  private final HealthApi api;

  @Inject
  public HealthSteps(HealthApi api) {
    this.api = api;
  }

  @Step("Check restful-booker is alive")
  public void ping() {
    Calls.expectStatus(api.ping(), 201);
  }

  @Step("Wait until restful-booker is up")
  public void waitUntilUp() {
    Waits.awaitSlow("restful-booker /ping answers 201")
        .until(() -> Calls.response(api.ping()).code() == 201);
  }
}
