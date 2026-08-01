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
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class BookingApplication implements AutoCloseable {

  private final ObjectMapper mapper;
  private final BookingRepository repository;
  private final HttpServer server;
  private final ExecutorService executor;

  private BookingApplication(int port, DatabaseConfig database) throws IOException {
    mapper =
        new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    repository = new BookingRepository(database);
    server = HttpServer.create(new InetSocketAddress(port), 0);
    executor = Executors.newVirtualThreadPerTaskExecutor();
    server.setExecutor(executor);
    server.createContext("/health", this::health);
    server.createContext("/api/bookings", this::bookings);
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

  private void write(HttpExchange exchange, int status, Object body) throws IOException {
    byte[] payload = mapper.writeValueAsString(body).getBytes(StandardCharsets.UTF_8);
    exchange.getResponseHeaders().set("Content-Type", "application/json");
    exchange.sendResponseHeaders(status, payload.length);
    try (OutputStream output = exchange.getResponseBody()) {
      output.write(payload);
    }
  }

  @Override
  public void close() {
    server.stop(1);
    executor.close();
    repository.close();
  }
}
