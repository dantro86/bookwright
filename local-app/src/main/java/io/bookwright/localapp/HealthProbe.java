package io.bookwright.localapp;

import java.net.HttpURLConnection;
import java.net.URI;

public final class HealthProbe {

  private HealthProbe() {}

  public static void main(String[] args) throws Exception {
    HttpURLConnection connection =
        (HttpURLConnection) URI.create("http://127.0.0.1:8080/health").toURL().openConnection();
    connection.setConnectTimeout(1_000);
    connection.setReadTimeout(1_000);
    connection.setRequestMethod("GET");
    if (connection.getResponseCode() != 200) {
      throw new IllegalStateException("Local booking application is not healthy");
    }
  }
}
