package io.bookwright.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.MediaType;

/** Removes credentials from HTTP diagnostics without modifying the actual request or response. */
public final class SecretSanitizer {

  static final String REDACTED = "[REDACTED]";
  static final String OMITTED_BODY = "[BODY OMITTED]";

  private static final Set<String> SENSITIVE_MARKERS =
      Set.of("authorization", "cookie", "credential", "password", "passwd", "token", "secret");

  private static final ObjectMapper JSON = new ObjectMapper();

  private SecretSanitizer() {}

  public static String url(HttpUrl url) {
    HttpUrl.Builder safe = url.newBuilder();
    url.queryParameterNames().stream()
        .filter(SecretSanitizer::isSensitive)
        .forEach(name -> safe.setQueryParameter(name, REDACTED));
    return safe.build().toString();
  }

  public static String url(String url) {
    try {
      return url(HttpUrl.get(url));
    } catch (IllegalArgumentException e) {
      return "[URL OMITTED]";
    }
  }

  public static String headers(Headers headers) {
    if (headers.size() == 0) {
      return "<none>";
    }
    return headers.names().stream()
        .map(
            name ->
                name
                    + ": "
                    + (isSensitive(name) ? REDACTED : String.join(", ", headers.values(name))))
        .collect(Collectors.joining(System.lineSeparator()));
  }

  public static String body(String body, MediaType mediaType) {
    if (body == null || body.isBlank()) {
      return "<empty>";
    }
    if (mediaType == null) {
      return OMITTED_BODY;
    }

    String subtype = mediaType.subtype().toLowerCase(Locale.ROOT);
    if (subtype.equals("json") || subtype.endsWith("+json")) {
      return sanitizeJson(body);
    }
    if (mediaType.type().equalsIgnoreCase("application")
        && subtype.equals("x-www-form-urlencoded")) {
      return sanitizeForm(body);
    }
    return OMITTED_BODY;
  }

  private static String sanitizeJson(String body) {
    try {
      JsonNode root = JSON.readTree(body);
      redact(root);
      return JSON.writeValueAsString(root);
    } catch (JsonProcessingException e) {
      return OMITTED_BODY;
    }
  }

  private static String sanitizeForm(String body) {
    try {
      return Arrays.stream(body.split("&"))
          .map(SecretSanitizer::sanitizeFormEntry)
          .collect(Collectors.joining("&"));
    } catch (IllegalArgumentException e) {
      return OMITTED_BODY;
    }
  }

  private static String sanitizeFormEntry(String entry) {
    String[] pair = entry.split("=", 2);
    String decodedName = URLDecoder.decode(pair[0], StandardCharsets.UTF_8);
    if (!isSensitive(decodedName) || pair.length == 1) {
      return entry;
    }
    return pair[0] + "=" + URLEncoder.encode(REDACTED, StandardCharsets.UTF_8);
  }

  private static void redact(JsonNode node) {
    if (node instanceof ObjectNode object) {
      object
          .properties()
          .forEach(
              entry -> {
                if (isSensitive(entry.getKey())) {
                  object.put(entry.getKey(), REDACTED);
                } else {
                  redact(entry.getValue());
                }
              });
    } else if (node instanceof ArrayNode array) {
      array.forEach(SecretSanitizer::redact);
    }
  }

  private static boolean isSensitive(String name) {
    String normalized = name.toLowerCase(Locale.ROOT).replace('-', '_');
    return SENSITIVE_MARKERS.stream().anyMatch(normalized::contains)
        || normalized.contains("api_key")
        || normalized.contains("apikey");
  }
}
