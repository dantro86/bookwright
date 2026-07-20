package io.bookwright.util;

import java.io.IOException;
import java.io.UncheckedIOException;
import lombok.experimental.UtilityClass;
import retrofit2.Call;
import retrofit2.Response;

/**
 * Executes a Retrofit call, asserts the status code and returns the body.
 * Keeps steps free of try/catch and status-check noise.
 */
@UtilityClass
public class Calls {

    public <T> T unwrap(Call<T> call, int expectedStatus) {
        Response<T> response = execute(call);
        checkStatus(response, expectedStatus);
        return response.body();
    }

    public <T> Response<T> execute(Call<T> call) {
        try {
            return call.execute();
        } catch (IOException e) {
            throw new UncheckedIOException("HTTP call failed: " + call.request().method() + " " + call.request().url(), e);
        }
    }

    public void checkStatus(Response<?> response, int expectedStatus) {
        if (response.code() != expectedStatus) {
            String body = "";
            try (var errorBody = response.errorBody()) {
                if (errorBody != null) {
                    body = errorBody.string();
                }
            } catch (IOException ignored) {
                // error body is best-effort diagnostic info
            }
            throw new AssertionError("Expected status %d but got %d for %s %s. Body: %s".formatted(
                    expectedStatus, response.code(),
                    response.raw().request().method(), response.raw().request().url(), body));
        }
    }
}
