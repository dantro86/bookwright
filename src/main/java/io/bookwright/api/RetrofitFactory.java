package io.bookwright.api;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.Duration;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

/**
 * The single place where HTTP is configured: timeouts, logging and Allure request/response
 * attachments. Everything above this is a plain Retrofit interface.
 */
public final class RetrofitFactory {

  private RetrofitFactory() {}

  public static Retrofit create(String baseUrl) {
    OkHttpClient client =
        new OkHttpClient.Builder()
            .connectTimeout(Duration.ofSeconds(15))
            .readTimeout(Duration.ofSeconds(30))
            .retryOnConnectionFailure(false)
            // restful-booker answers 418 to requests without an Accept header
            .addInterceptor(
                chain ->
                    chain.proceed(
                        chain.request().newBuilder().header("Accept", "application/json").build()))
            .addInterceptor(new SafeHttpReportingInterceptor())
            .build();

    return new Retrofit.Builder()
        .baseUrl(baseUrl.endsWith("/") ? baseUrl : baseUrl + "/")
        .client(client)
        .addConverterFactory(JacksonConverterFactory.create(objectMapper()))
        .build();
  }

  private static ObjectMapper objectMapper() {
    return new ObjectMapper()
        .registerModule(new JavaTimeModule())
        .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
  }
}
