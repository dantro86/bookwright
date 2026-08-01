package io.bookwright.steps;

import com.google.inject.Inject;
import io.bookwright.api.LocalBookingApi;
import io.bookwright.api.model.LocalBooking;
import io.bookwright.teardown.TeardownStorage;
import io.bookwright.util.Calls;
import io.qameta.allure.Step;

public class LocalBookingApiSteps {

  private final LocalBookingApi api;
  private final TeardownStorage teardown;

  @Inject
  public LocalBookingApiSteps(LocalBookingApi api, TeardownStorage teardown) {
    this.api = api;
    this.teardown = teardown;
  }

  @Step("Create a booking through the local application API")
  public LocalBooking create(LocalBooking booking) {
    LocalBooking created = Calls.body(api.create(booking), 201, "created local booking");
    teardown.push(
        "delete local booking " + created.getId(), () -> deleteIfPresent(created.getId()));
    return created;
  }

  @Step("Get local booking {id}")
  public LocalBooking get(int id) {
    return Calls.body(api.get(id), 200, "local booking " + id);
  }

  @Step("Delete local booking {id}")
  public void delete(int id) {
    Calls.expectStatus(api.delete(id), 204);
  }

  private void deleteIfPresent(int id) {
    int status = Calls.response(api.delete(id)).code();
    if (status != 204 && status != 404) {
      throw new IllegalStateException(
          "Cleanup of local booking %d returned HTTP %d".formatted(id, status));
    }
  }
}
