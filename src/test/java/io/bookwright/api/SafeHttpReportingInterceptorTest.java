package io.bookwright.api;

import java.io.IOException;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SafeHttpReportingInterceptorTest {

    private MockWebServer server;

    @BeforeEach
    void startServer() throws IOException {
        server = new MockWebServer();
        server.start();
    }

    @AfterEach
    void stopServer() throws IOException {
        server.shutdown();
    }

    @Test
    void redactsSensitiveRequestHeadersQueryParametersAndNestedJsonFields() throws IOException {
        Request request = new Request.Builder()
                .url(server.url("/auth?token=query-secret&source=bookwright"))
                .header("Authorization", "Bearer header-secret")
                .header("Cookie", "session=cookie-secret")
                .header("X-Api-Key", "custom-header-secret")
                .header("X-Correlation-Id", "visible-correlation-id")
                .post(RequestBody.create(
                        """
                        {
                          "username": "danil",
                          "password": "body-secret",
                          "metadata": {"access_token": "nested-secret", "purpose": "demo"}
                        }
                        """,
                        MediaType.get("application/json")))
                .build();

        String report = SafeHttpReportingInterceptor.requestReport(request);

        assertThat(report)
                .contains("source=bookwright", "visible-correlation-id", "danil", "demo", SecretSanitizer.REDACTED)
                .doesNotContain(
                        "query-secret",
                        "header-secret",
                        "cookie-secret",
                        "custom-header-secret",
                        "body-secret",
                        "nested-secret");
    }

    @Test
    void redactsSensitiveResponseHeadersAndJsonFields() throws IOException {
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json")
                .addHeader("Set-Cookie", "session=response-cookie-secret")
                .addHeader("X-Request-Id", "visible-request-id")
                .setBody("{\"token\":\"response-token-secret\",\"status\":\"ready\"}"));
        Request request = new Request.Builder().url(server.url("/auth")).build();

        try (Response response = new OkHttpClient().newCall(request).execute()) {
            String report = SafeHttpReportingInterceptor.responseReport(response, 12);

            assertThat(report)
                    .contains("Status: 200", "Duration: 12 ms", "visible-request-id", "ready", SecretSanitizer.REDACTED)
                    .doesNotContain("response-cookie-secret", "response-token-secret");
        }
    }

    @Test
    void omitsUnsupportedAndMalformedBodiesInsteadOfRiskingSecretExposure() {
        assertThat(SecretSanitizer.body("plain-secret", MediaType.get("text/plain")))
                .isEqualTo(SecretSanitizer.OMITTED_BODY);
        assertThat(SecretSanitizer.body("{not-json", MediaType.get("application/json")))
                .isEqualTo(SecretSanitizer.OMITTED_BODY);
    }

    @Test
    void redactsFormCredentials() {
        String sanitized = SecretSanitizer.body(
                "username=danil&password=form-secret&scope=read",
                MediaType.get("application/x-www-form-urlencoded"));

        assertThat(sanitized)
                .contains("username=danil", "scope=read")
                .contains("password=%5BREDACTED%5D")
                .doesNotContain("form-secret");
    }

    @Test
    void omitsMalformedFormInsteadOfFailingReporting() {
        assertThat(SecretSanitizer.body(
                "%invalid=form-secret",
                MediaType.get("application/x-www-form-urlencoded")))
                .isEqualTo(SecretSanitizer.OMITTED_BODY);
    }
}
