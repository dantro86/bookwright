package io.bookwright.localapp;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class BookingApplication implements AutoCloseable {

  private final ObjectMapper mapper;
  private final ApplicationDatabase database;
  private final BookingRepository repository;
  private final UserRepository users;
  private final HttpServer server;
  private final ExecutorService executor;

  private BookingApplication(int port, DatabaseConfig database) throws IOException {
    mapper =
        new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    this.database = new ApplicationDatabase(database);
    repository = new BookingRepository(this.database.dataSource());
    users = new UserRepository(this.database.dataSource());
    try {
      users.ensureExistingUser(database);
    } catch (SQLException exception) {
      this.database.close();
      throw new IllegalStateException(
          "Could not initialize the configured existing user", exception);
    }
    server = HttpServer.create(new InetSocketAddress(port), 0);
    executor = Executors.newVirtualThreadPerTaskExecutor();
    server.setExecutor(executor);
    server.createContext("/health", this::health);
    server.createContext("/api/bookings", this::bookings);
    server.createContext("/api/users", this::userRegistration);
    server.createContext("/api/auth/sessions", this::sessions);
    server.createContext("/app/bookings", this::bookingsPage);
  }

  public static void main(String[] args) throws IOException {
    int port = Integer.parseInt(System.getenv().getOrDefault("APP_PORT", "8080"));
    BookingApplication application = new BookingApplication(port, DatabaseConfig.fromEnvironment());
    Runtime.getRuntime().addShutdownHook(new Thread(application::close));
    application.start();
  }

  private void start() {
    server.start();
    System.out.println(
        "Local booking application listening on port " + server.getAddress().getPort());
  }

  private void health(HttpExchange exchange) throws IOException {
    if (!exchange.getRequestMethod().equals("GET")) {
      write(exchange, 405, Map.of("error", "Method not allowed"));
      return;
    }
    if (repository.isReady()) {
      write(exchange, 200, Map.of("status", "UP"));
    } else {
      write(exchange, 503, Map.of("status", "DOWN"));
    }
  }

  private void bookings(HttpExchange exchange) throws IOException {
    try {
      String path = exchange.getRequestURI().getPath();
      if (path.equals("/api/bookings") && exchange.getRequestMethod().equals("POST")) {
        create(exchange);
        return;
      }
      Integer id = bookingId(path);
      if (id == null) {
        write(exchange, 404, Map.of("error", "Route not found"));
      } else if (exchange.getRequestMethod().equals("GET")) {
        find(exchange, id);
      } else if (exchange.getRequestMethod().equals("DELETE")) {
        delete(exchange, id);
      } else {
        write(exchange, 405, Map.of("error", "Method not allowed"));
      }
    } catch (IllegalArgumentException exception) {
      write(exchange, 400, Map.of("error", exception.getMessage()));
    } catch (SQLException exception) {
      write(exchange, 500, Map.of("error", "Database operation failed"));
    }
  }

  private void userRegistration(HttpExchange exchange) throws IOException {
    try {
      if (exchange.getRequestURI().getPath().equals("/api/users")
          && exchange.getRequestMethod().equals("POST")) {
        UserRegistrationRequest request =
            mapper.readValue(exchange.getRequestBody(), UserRegistrationRequest.class);
        validate(request);
        write(exchange, 201, users.create(request));
        return;
      }
      if (exchange.getRequestURI().getPath().equals("/api/users/me")
          && exchange.getRequestMethod().equals("DELETE")) {
        String token = bearerToken(exchange);
        if (token == null) {
          write(exchange, 401, Map.of("error", "Authentication required"));
        } else if (users.deleteOwnUser(token)) {
          exchange.sendResponseHeaders(204, -1);
          exchange.close();
        } else {
          write(exchange, 401, Map.of("error", "Session is invalid or expired"));
        }
        return;
      }
      write(exchange, 404, Map.of("error", "Route not found"));
    } catch (SQLIntegrityConstraintViolationException exception) {
      write(exchange, 409, Map.of("error", "A user with this email already exists"));
    } catch (IllegalArgumentException exception) {
      write(exchange, 400, Map.of("error", exception.getMessage()));
    } catch (SQLException exception) {
      write(exchange, 500, Map.of("error", "Database operation failed"));
    }
  }

  private void sessions(HttpExchange exchange) throws IOException {
    if (!exchange.getRequestURI().getPath().equals("/api/auth/sessions")
        || !exchange.getRequestMethod().equals("POST")) {
      write(exchange, 404, Map.of("error", "Route not found"));
      return;
    }
    try {
      UserLoginRequest request =
          mapper.readValue(exchange.getRequestBody(), UserLoginRequest.class);
      UserSessionResponse session = users.login(request);
      if (session == null) {
        write(exchange, 401, Map.of("error", "Invalid credentials"));
      } else {
        write(exchange, 200, session);
      }
    } catch (SQLException exception) {
      write(exchange, 500, Map.of("error", "Database operation failed"));
    }
  }

  private void bookingsPage(HttpExchange exchange) throws IOException {
    if (!exchange.getRequestMethod().equals("GET")) {
      write(exchange, 405, Map.of("error", "Method not allowed"));
      return;
    }
    try {
      String token = sessionCookie(exchange);
      UserResponse user = token == null ? null : users.findByToken(token);
      if (user == null) {
        writeHtml(
            exchange,
            401,
            "<main><h1>Authentication required</h1><p data-testid='auth-error'>Session is missing, invalid, or expired.</p></main>");
        return;
      }
      writeHtml(
          exchange,
          200,
          """
          <!doctype html>
          <html lang="en"><head><meta charset="utf-8"><title>Bookwright bookings</title></head>
          <body><main>
            <h1>Bookings</h1>
            <p>Signed in as <strong data-testid="current-user">%s</strong></p>
            <p data-testid="welcome-message">Welcome, %s</p>
          </main></body></html>
          """
              .formatted(escape(user.email()), escape(user.displayName())));
    } catch (SQLException exception) {
      write(exchange, 500, Map.of("error", "Database operation failed"));
    }
  }

  private void create(HttpExchange exchange) throws IOException, SQLException {
    BookingRequest request = mapper.readValue(exchange.getRequestBody(), BookingRequest.class);
    validate(request);
    write(exchange, 201, repository.create(request));
  }

  private void find(HttpExchange exchange, int id) throws IOException, SQLException {
    BookingResponse booking = repository.find(id);
    if (booking == null) {
      write(exchange, 404, Map.of("error", "Booking not found"));
    } else {
      write(exchange, 200, booking);
    }
  }

  private void delete(HttpExchange exchange, int id) throws IOException, SQLException {
    if (repository.delete(id)) {
      exchange.sendResponseHeaders(204, -1);
      exchange.close();
    } else {
      write(exchange, 404, Map.of("error", "Booking not found"));
    }
  }

  private Integer bookingId(String path) {
    String prefix = "/api/bookings/";
    if (!path.startsWith(prefix)) {
      return null;
    }
    String value = path.substring(prefix.length());
    if (value.isBlank() || value.contains("/")) {
      return null;
    }
    try {
      return Integer.valueOf(value);
    } catch (NumberFormatException exception) {
      throw new IllegalArgumentException("Booking id must be an integer");
    }
  }

  private void validate(BookingRequest booking) {
    if (booking.roomId() == null
        || booking.guestFirstName() == null
        || booking.guestFirstName().isBlank()
        || booking.guestLastName() == null
        || booking.guestLastName().isBlank()
        || booking.checkin() == null
        || booking.checkout() == null
        || booking.depositPaid() == null) {
      throw new IllegalArgumentException("All booking fields are required");
    }
    if (!booking.checkout().isAfter(booking.checkin())) {
      throw new IllegalArgumentException("Checkout must be after checkin");
    }
  }

  private void validate(UserRegistrationRequest user) {
    if (user.email() == null
        || user.email().isBlank()
        || !user.email().contains("@")
        || user.password() == null
        || user.password().length() < 10
        || user.displayName() == null
        || user.displayName().isBlank()) {
      throw new IllegalArgumentException(
          "Email, display name, and a password of at least 10 characters are required");
    }
  }

  private String bearerToken(HttpExchange exchange) {
    String authorization = exchange.getRequestHeaders().getFirst("Authorization");
    return authorization != null && authorization.startsWith("Bearer ")
        ? authorization.substring("Bearer ".length())
        : null;
  }

  private String sessionCookie(HttpExchange exchange) {
    String cookie = exchange.getRequestHeaders().getFirst("Cookie");
    if (cookie == null) {
      return null;
    }
    return Arrays.stream(cookie.split(";"))
        .map(String::trim)
        .filter(value -> value.startsWith("bookwright_session="))
        .map(value -> value.substring("bookwright_session=".length()))
        .findFirst()
        .orElse(null);
  }

  private String escape(String value) {
    return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
  }

  private void write(HttpExchange exchange, int status, Object body) throws IOException {
    byte[] payload = mapper.writeValueAsString(body).getBytes(StandardCharsets.UTF_8);
    exchange.getResponseHeaders().set("Content-Type", "application/json");
    exchange.sendResponseHeaders(status, payload.length);
    try (OutputStream output = exchange.getResponseBody()) {
      output.write(payload);
    }
  }

  private void writeHtml(HttpExchange exchange, int status, String html) throws IOException {
    byte[] payload = html.getBytes(StandardCharsets.UTF_8);
    exchange.getResponseHeaders().set("Content-Type", "text/html; charset=utf-8");
    exchange.sendResponseHeaders(status, payload.length);
    try (OutputStream output = exchange.getResponseBody()) {
      output.write(payload);
    }
  }

  @Override
  public void close() {
    server.stop(1);
    executor.close();
    database.close();
  }
}
