package io.bookwright.api;

import static org.assertj.core.api.Assertions.assertThat;

import okhttp3.OkHttpClient;
import org.junit.jupiter.api.Test;
import retrofit2.Retrofit;

class RetrofitFactoryTest {

  @Test
  void doesNotApplyGlobalConnectionRetries() {
    Retrofit retrofit = RetrofitFactory.create("https://example.test");

    assertThat(retrofit.callFactory())
        .isInstanceOfSatisfying(
            OkHttpClient.class, client -> assertThat(client.retryOnConnectionFailure()).isFalse());
  }
}
