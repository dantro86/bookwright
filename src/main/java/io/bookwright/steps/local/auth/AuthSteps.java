package io.bookwright.steps.local.auth;

import com.google.inject.Inject;
import io.bookwright.api.local.auth.AuthApi;
import io.bookwright.api.model.UserCredentials;
import io.bookwright.api.model.UserSession;
import io.bookwright.util.Calls;
import io.qameta.allure.Step;

public class AuthSteps {

  private final AuthApi api;

  @Inject
  public AuthSteps(AuthApi api) {
    this.api = api;
  }

  @Step("Authenticate local application user {credentials.email}")
  public UserSession login(UserCredentials credentials) {
    return Calls.body(api.login(credentials), 200, "authenticated user session");
  }

  @Step("Reject invalid credentials for local application user {credentials.email}")
  public void expectLoginRejected(UserCredentials credentials) {
    Calls.expectStatus(api.login(credentials), 401);
  }
}
