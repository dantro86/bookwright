package io.bookwright.steps.restfulbooker.auth;

import com.google.inject.Inject;
import io.bookwright.api.AuthSession;
import io.bookwright.api.UnexpectedResponseException;
import io.bookwright.api.model.AuthRequest;
import io.bookwright.api.model.AuthResponse;
import io.bookwright.api.restfulbooker.auth.AuthApi;
import io.bookwright.config.MainConfig;
import io.bookwright.util.Calls;
import io.qameta.allure.Step;

public class AuthSteps {

  private final AuthApi api;
  private final MainConfig config;
  private volatile String cachedToken;

  @Inject
  public AuthSteps(AuthApi api, MainConfig config) {
    this.api = api;
    this.config = config;
  }

  @Step("Get restful-booker auth token")
  public String token() {
    if (cachedToken == null) {
      AuthResponse response =
          Calls.body(
              api.createToken(
                  AuthRequest.builder()
                      .username(config.apiUsername())
                      .password(config.apiPassword())
                      .build()),
              200,
              "auth token response");
      if (response.getToken() == null || response.getToken().isBlank()) {
        throw new UnexpectedResponseException("Auth response token was blank");
      }
      cachedToken = response.getToken();
    }
    return cachedToken;
  }

  @Step("Create authenticated restful-booker session")
  public AuthSession session() {
    return new AuthSession(token());
  }
}
