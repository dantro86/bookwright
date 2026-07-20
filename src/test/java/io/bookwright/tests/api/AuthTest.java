package io.bookwright.tests.api;

import io.bookwright.annotations.Api;
import io.bookwright.annotations.OwnerDanil;
import io.bookwright.annotations.Smoke;
import io.bookwright.steps.ApiSteps;
import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Api
@Smoke
@OwnerDanil
@Feature("Auth")
class AuthTest {

    @Test
    @DisplayName("API responds to ping")
    void apiIsAlive(ApiSteps api) {
        api.auth().ping();
    }

    @Test
    @DisplayName("API becomes available within the warm-up window")
    void apiBecomesAvailable(ApiSteps api) {
        api.auth().waitUntilApiUp();
    }

    @Test
    @DisplayName("Auth token is issued for valid credentials")
    void tokenIsIssued(ApiSteps api) {
        assertThat(api.auth().token()).isNotBlank();
    }
}
