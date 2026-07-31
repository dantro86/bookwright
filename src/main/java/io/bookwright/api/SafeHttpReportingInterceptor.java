package io.bookwright.api;

import io.qameta.allure.Allure;
import java.io.IOException;
import java.time.Duration;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;

/**
 * Produces useful HTTP logs and Allure attachments without exposing credentials.
 * The original request and response are never modified.
 */
@Slf4j
public final class SafeHttpReportingInterceptor implements Interceptor {

    private static final long MAX_CAPTURE_BYTES = 64 * 1024;

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        String safeUrl = SecretSanitizer.url(request.url());
        long startedAt = System.nanoTime();

        log.info("HTTP --> {} {}", request.method(), safeUrl);
        attachRequest(request);

        try {
            Response response = chain.proceed(request);
            long elapsedMillis = Duration.ofNanos(System.nanoTime() - startedAt).toMillis();
            log.info("HTTP <-- {} {} {} ({} ms)", response.code(), request.method(), safeUrl, elapsedMillis);
            attachResponse(response, elapsedMillis);
            return response;
        } catch (IOException e) {
            long elapsedMillis = Duration.ofNanos(System.nanoTime() - startedAt).toMillis();
            log.warn("HTTP <-- transport failure {} {} ({} ms): {}",
                    request.method(), safeUrl, elapsedMillis, e.getClass().getSimpleName());
            throw e;
        }
    }

    private static void attachRequest(Request request) {
        try {
            Allure.addAttachment(
                    "HTTP request: " + request.method() + " " + request.url().encodedPath(),
                    "text/plain",
                    requestReport(request),
                    ".txt");
        } catch (Exception e) {
            log.warn("Safe HTTP request report could not be created: {}", e.getClass().getSimpleName());
        }
    }

    private static void attachResponse(Response response, long elapsedMillis) {
        try {
            Allure.addAttachment(
                    "HTTP response: " + response.code() + " " + response.request().url().encodedPath(),
                    "text/plain",
                    responseReport(response, elapsedMillis),
                    ".txt");
        } catch (Exception e) {
            log.warn("Safe HTTP response report could not be created: {}", e.getClass().getSimpleName());
        }
    }

    static String requestReport(Request request) throws IOException {
        return "Method: %s%nURL: %s%n%nHeaders:%n%s%n%nBody:%n%s".formatted(
                request.method(),
                SecretSanitizer.url(request.url()),
                SecretSanitizer.headers(request.headers()),
                requestBody(request.body()));
    }

    static String responseReport(Response response, long elapsedMillis) throws IOException {
        ResponseBody body = response.peekBody(MAX_CAPTURE_BYTES);
        return "Status: %d%nDuration: %d ms%nURL: %s%n%nHeaders:%n%s%n%nBody:%n%s".formatted(
                response.code(),
                elapsedMillis,
                SecretSanitizer.url(response.request().url()),
                SecretSanitizer.headers(response.headers()),
                SecretSanitizer.body(body.string(), body.contentType()));
    }

    private static String requestBody(RequestBody body) throws IOException {
        if (body == null) {
            return "<empty>";
        }
        if (body.isDuplex() || body.isOneShot() || body.contentLength() > MAX_CAPTURE_BYTES) {
            return SecretSanitizer.OMITTED_BODY;
        }
        Buffer buffer = new Buffer();
        body.writeTo(buffer);
        return SecretSanitizer.body(buffer.readUtf8(), body.contentType());
    }
}
