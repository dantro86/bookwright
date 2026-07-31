package io.bookwright.steps;

import com.google.inject.Inject;
import io.bookwright.api.AuthApi;
import io.bookwright.api.AuthSession;
import io.bookwright.api.UnexpectedResponseException;
import io.bookwright.api.model.AuthRequest;
import io.bookwright.api.model.AuthResponse;
import io.bookwright.config.MainConfig;
import io.bookwright.util.Calls;
import io.bookwright.util.Waits;
import io.qameta.allure.Step;

public class AuthApiSteps {

    private final AuthApi authApi;
    private final MainConfig config;
    private volatile String cachedToken;

    @Inject
    public AuthApiSteps(AuthApi authApi, MainConfig config) {
        this.authApi = authApi;
        this.config = config;
    }

    @Step("Get auth token")
    public String token() {
        if (cachedToken == null) {
            AuthResponse response = Calls.body(authApi.createToken(AuthRequest.builder()
                    .username(config.apiUsername())
                    .password(config.apiPassword())
                    .build()), 200, "auth token response");
            if (response.getToken() == null || response.getToken().isBlank()) {
                throw new UnexpectedResponseException("Auth response token was blank");
            }
            cachedToken = response.getToken();
        }
        return cachedToken;
    }

    @Step("Create authenticated API session")
    public AuthSession session() {
        return new AuthSession(token());
    }

    @Step("Check API is alive")
    public void ping() {
        Calls.expectStatus(authApi.ping(), 201);
    }

    /**
     * Health-wait example: polls /ping until the service answers. Useful right
     * after `docker compose up` or against a cold heroku dyno, where the first
     * requests may fail or hang.
     */
    @Step("Wait until API is up")
    public void waitUntilApiUp() {
        Waits.awaitSlow("API /ping answers 201")
                .until(() -> Calls.response(authApi.ping()).code() == 201);
    }
}
