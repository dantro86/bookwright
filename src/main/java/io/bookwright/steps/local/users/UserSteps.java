package io.bookwright.steps.local.users;

import com.google.inject.Inject;
import io.bookwright.api.local.users.UsersApi;
import io.bookwright.api.model.UserProfile;
import io.bookwright.api.model.UserRegistration;
import io.bookwright.api.model.UserSession;
import io.bookwright.teardown.TeardownStorage;
import io.bookwright.util.Calls;
import io.qameta.allure.Step;

public class UserSteps {

  private final UsersApi api;
  private final TeardownStorage teardown;

  @Inject
  public UserSteps(UsersApi api, TeardownStorage teardown) {
    this.api = api;
    this.teardown = teardown;
  }

  @Step("Register a new local application user")
  public UserProfile register(UserRegistration registration) {
    return Calls.body(api.register(registration), 201, "registered user");
  }

  public void registerCleanup(UserSession session) {
    teardown.push("delete test user " + session.user().email(), () -> deleteIfPresent(session));
  }

  private void deleteIfPresent(UserSession session) {
    int status = Calls.response(api.deleteCurrentUser(session.bearerToken())).code();
    if (status != 204 && status != 401) {
      throw new IllegalStateException(
          "Cleanup of user %s returned HTTP %d".formatted(session.user().email(), status));
    }
  }
}
