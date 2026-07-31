package io.bookwright.tests.framework;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.bookwright.api.ApiCallException;
import io.bookwright.api.UnexpectedResponseException;
import io.bookwright.util.Calls;
import io.bookwright.util.Waits;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.SocketPolicy;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;
import retrofit2.http.GET;

class CallsTest {

  private MockWebServer server;
  private TestApi api;

  @BeforeEach
  void startServer() throws IOException {
    server = new MockWebServer();
    server.start();
    api =
        new Retrofit.Builder()
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
    server.enqueue(
        new MockResponse()
            .setResponseCode(503)
            .addHeader("Content-Type", "application/json")
            .setBody("{\"message\":\"temporarily unavailable\",\"token\":\"server-secret\"}"));

    assertThatThrownBy(() -> Calls.expectStatus(api.get(), 200))
        .isInstanceOf(UnexpectedResponseException.class)
        .hasMessageContaining("Expected status [200] but got 503")
        .hasMessageContaining("GET")
        .hasMessageContaining("temporarily unavailable")
        .satisfies(error -> assertThat(error.getMessage()).doesNotContain("server-secret"));
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

  @Test
  void reportsMalformedJsonAsCallFailure() {
    server.enqueue(
        new MockResponse()
            .setResponseCode(200)
            .addHeader("Content-Type", "application/json")
            .setBody("{\"value\":"));

    assertThatThrownBy(() -> Calls.body(api.get(), 200, "test response"))
        .isInstanceOf(ApiCallException.class)
        .hasMessageContaining("HTTP call failed")
        .hasCauseInstanceOf(IOException.class);
  }

  @Test
  void reportsReadTimeoutAsTransportFailure() {
    server.enqueue(
        new MockResponse()
            .setResponseCode(200)
            .setBody("{\"value\":\"late\"}")
            .setBodyDelay(250, TimeUnit.MILLISECONDS));
    api =
        api(
            new OkHttpClient.Builder()
                .readTimeout(Duration.ofMillis(50))
                .retryOnConnectionFailure(false)
                .build());

    assertThatThrownBy(() -> Calls.body(api.get(), 200, "slow response"))
        .isInstanceOf(ApiCallException.class)
        .hasCauseInstanceOf(SocketTimeoutException.class);
  }

  @Test
  void callHelperDoesNotRetryDisconnectedRequest() {
    server.enqueue(new MockResponse().setSocketPolicy(SocketPolicy.DISCONNECT_AT_START));
    server.enqueue(success("explicit retry"));
    api = api(new OkHttpClient.Builder().retryOnConnectionFailure(false).build());

    assertThatThrownBy(() -> Calls.body(api.get(), 200, "disconnected response"))
        .isInstanceOf(ApiCallException.class);
    assertThat(server.getRequestCount()).isEqualTo(1);

    assertThat(Calls.body(api.get(), 200, "explicit retry").value()).isEqualTo("explicit retry");
    assertThat(server.getRequestCount()).isEqualTo(2);
  }

  @Test
  void awaitilityRetriesTransientCallFailureAtTheCallSite() {
    server.enqueue(new MockResponse().setSocketPolicy(SocketPolicy.DISCONNECT_AT_START));
    server.enqueue(success("ready"));
    api = api(new OkHttpClient.Builder().retryOnConnectionFailure(false).build());

    Waits.await("test endpoint becomes available")
        .pollInterval(Duration.ofMillis(5))
        .atMost(Duration.ofSeconds(1))
        .until(() -> Calls.body(api.get(), 200, "test response").value().equals("ready"));

    assertThat(server.getRequestCount()).isEqualTo(2);
  }

  @Test
  void awaitilityDoesNotRetryUnexpectedResponse() {
    server.enqueue(new MockResponse().setResponseCode(503).setBody("temporarily unavailable"));
    server.enqueue(success("must not be reached"));

    assertThatThrownBy(
            () ->
                Waits.await("test endpoint returns expected contract")
                    .pollInterval(Duration.ofMillis(5))
                    .atMost(Duration.ofSeconds(1))
                    .until(
                        () -> Calls.body(api.get(), 200, "test response").value().equals("ready")))
        .isInstanceOf(UnexpectedResponseException.class)
        .hasMessageContaining("got 503");
    assertThat(server.getRequestCount()).isEqualTo(1);
  }

  private TestApi api(OkHttpClient client) {
    return new Retrofit.Builder()
        .baseUrl(server.url("/"))
        .client(client)
        .addConverterFactory(JacksonConverterFactory.create())
        .build()
        .create(TestApi.class);
  }

  private MockResponse success(String value) {
    return new MockResponse()
        .setResponseCode(200)
        .addHeader("Content-Type", "application/json")
        .setBody("{\"value\":\"%s\"}".formatted(value));
  }

  private interface TestApi {
    @GET("status")
    Call<TestBody> get();
  }

  private record TestBody(String value) {}
}
