package io.bookwright.util;

import java.io.IOException;

import io.bookwright.api.ApiCallException;
import io.bookwright.api.SecretSanitizer;
import io.bookwright.api.UnexpectedResponseException;
import java.util.Arrays;
import lombok.experimental.UtilityClass;
import retrofit2.Call;
import retrofit2.Response;

/**
 * Focused Retrofit execution helpers: raw response, expected status, or required body.
 * Transport failures and response-contract failures remain distinct diagnostics.
 */
@UtilityClass
public class Calls {

    public <T> T body(Call<T> call, int expectedStatus, String description) {
        Response<T> response = expectStatus(call, expectedStatus);
        T body = response.body();
        if (body == null) {
            throw new UnexpectedResponseException(
                    "Expected %s but response body was empty for %s %s".formatted(
                            description,
                            response.raw().request().method(),
                            response.raw().request().url()));
        }
        return body;
    }

    public <T> Response<T> response(Call<T> call) {
        try {
            return call.execute();
        } catch (IOException e) {
            throw new ApiCallException(
                    "HTTP call failed: " + call.request().method() + " "
                            + SecretSanitizer.url(call.request().url()), e);
        }
    }

    public <T> Response<T> expectStatus(Call<T> call, int expectedStatus) {
        Response<T> response = response(call);
        expectStatus(response, expectedStatus);
        return response;
    }

    public void expectStatus(Response<?> response, int... expectedStatuses) {
        if (Arrays.stream(expectedStatuses).noneMatch(status -> status == response.code())) {
            throw new UnexpectedResponseException(responseDiagnostic(response, expectedStatuses));
        }
    }

    private String responseDiagnostic(Response<?> response, int[] expectedStatuses) {
        String errorBody = "";
        try (var body = response.errorBody()) {
            if (body != null) {
                errorBody = SecretSanitizer.body(body.string(), body.contentType());
            }
        } catch (IOException ignored) {
            // Error body is best-effort diagnostic information.
        }
        return "Expected status %s but got %d for %s %s. Body: %s".formatted(
                Arrays.toString(expectedStatuses), response.code(),
                response.raw().request().method(),
                SecretSanitizer.url(response.raw().request().url()),
                errorBody);
    }
}
