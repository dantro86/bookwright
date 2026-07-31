package io.bookwright.tests.framework;

import io.bookwright.api.ApiCallException;
import io.bookwright.api.UnexpectedResponseException;
import io.bookwright.util.Calls;
import java.io.IOException;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;
import retrofit2.http.GET;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CallsTest {

    private MockWebServer server;
    private TestApi api;

    @BeforeEach
    void startServer() throws IOException {
        server = new MockWebServer();
        server.start();
        api = new Retrofit.Builder()
                .baseUrl(server.url("/"))
                .addConverterFactory(JacksonConverterFactory.create())
                .build()
                .create(TestApi.class);
    }

    @AfterEach
    void stopServer() throws IOException {
        server.shutdown();
    }

    @Test
    void returnsBodyWhenResponseMatchesContract() {
        server.enqueue(new MockResponse().setResponseCode(200).setBody("{\"value\":\"ready\"}"));

        TestBody body = Calls.body(api.get(), 200, "test response");

        assertThat(body.value()).isEqualTo("ready");
    }

    @Test
    void reportsUnexpectedStatusWithRequestDiagnostics() {
        server.enqueue(new MockResponse().setResponseCode(503).setBody("temporarily unavailable"));

        assertThatThrownBy(() -> Calls.expectStatus(api.get(), 200))
                .isInstanceOf(UnexpectedResponseException.class)
                .hasMessageContaining("Expected status [200] but got 503")
                .hasMessageContaining("GET")
                .hasMessageContaining("temporarily unavailable");
    }

    @Test
    void rejectsSuccessfulResponseWithoutRequiredBody() {
        server.enqueue(new MockResponse().setResponseCode(204));

        assertThatThrownBy(() -> Calls.body(api.get(), 204, "test response"))
                .isInstanceOf(UnexpectedResponseException.class)
                .hasMessageContaining("response body was empty")
                .hasMessageContaining("test response");
    }

    @Test
    void separatesTransportFailureFromResponseContractFailure() throws IOException {
        Call<TestBody> call = api.get();
        server.shutdown();

        assertThatThrownBy(() -> Calls.response(call))
                .isInstanceOf(ApiCallException.class)
                .hasMessageContaining("HTTP call failed")
                .hasCauseInstanceOf(IOException.class);
    }

    private interface TestApi {
        @GET("status")
        Call<TestBody> get();
    }

    private record TestBody(String value) {
    }
}
