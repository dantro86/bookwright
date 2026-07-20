package io.bookwright.steps;

import com.google.inject.Inject;
import io.bookwright.api.AuthApi;
import io.bookwright.api.model.AuthRequest;
import io.bookwright.api.model.AuthResponse;
import io.bookwright.config.MainConfig;
import io.bookwright.util.Calls;
import io.qameta.allure.Step;
import retrofit2.Response;

import static org.assertj.core.api.Assertions.assertThat;

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
            AuthResponse response = Calls.unwrap(authApi.createToken(AuthRequest.builder()
                    .username(config.apiUsername())
                    .password(config.apiPassword())
                    .build()), 200);
            assertThat(response.getToken()).as("auth token").isNotBlank();
            cachedToken = response.getToken();
        }
        return cachedToken;
    }

    @Step("Check API is alive")
    public void ping() {
        Response<Void> response = Calls.execute(authApi.ping());
        Calls.checkStatus(response, 201);
    }
}
