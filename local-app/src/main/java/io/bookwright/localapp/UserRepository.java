package io.bookwright.localapp;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import javax.sql.DataSource;

final class UserRepository {

  private final DataSource dataSource;

  UserRepository(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  UserResponse ensureExistingUser(DatabaseConfig config) throws SQLException {
    UserRecord existing = findByEmail(config.existingUserEmail());
    if (existing != null) {
      return existing.response();
    }
    return create(
        new UserRegistrationRequest(
            config.existingUserEmail(), config.existingUserPassword(), config.existingUserName()));
  }

  UserResponse create(UserRegistrationRequest request) throws SQLException {
    String sql = "INSERT INTO app_user (email, display_name, password_hash) VALUES (?, ?, ?)";
    try (Connection connection = dataSource.getConnection();
        PreparedStatement statement =
            connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
      statement.setString(1, normalizeEmail(request.email()));
      statement.setString(2, request.displayName().trim());
      statement.setString(3, PasswordHasher.hashPassword(request.password()));
      statement.executeUpdate();
      try (ResultSet keys = statement.getGeneratedKeys()) {
        if (!keys.next()) {
          throw new SQLException("MySQL did not return a generated user id");
        }
        return new UserResponse(
            keys.getInt(1), normalizeEmail(request.email()), request.displayName().trim());
      }
    }
  }

  UserSessionResponse login(UserLoginRequest request) throws SQLException {
    UserRecord user = findByEmail(request.email());
    if (user == null || !PasswordHasher.matches(request.password(), user.passwordHash())) {
      return null;
    }
    String token = UUID.randomUUID().toString();
    Instant expiresAt = Instant.now().plus(1, ChronoUnit.HOURS);
    try (Connection connection = dataSource.getConnection();
        PreparedStatement statement =
            connection.prepareStatement(
                "INSERT INTO user_session (token_hash, user_id, expires_at) VALUES (?, ?, ?)")) {
      statement.setString(1, PasswordHasher.tokenHash(token));
      statement.setInt(2, user.response().id());
      statement.setTimestamp(3, Timestamp.from(expiresAt));
      statement.executeUpdate();
    }
    return new UserSessionResponse(token, expiresAt, user.response());
  }

  UserResponse findByToken(String token) throws SQLException {
    String sql =
        """
        SELECT u.id, u.email, u.display_name
        FROM user_session s
        JOIN app_user u ON u.id = s.user_id
        WHERE s.token_hash = ? AND s.expires_at > CURRENT_TIMESTAMP
        """;
    try (Connection connection = dataSource.getConnection();
        PreparedStatement statement = connection.prepareStatement(sql)) {
      statement.setString(1, PasswordHasher.tokenHash(token));
      try (ResultSet result = statement.executeQuery()) {
        return result.next()
            ? new UserResponse(
                result.getInt("id"), result.getString("email"), result.getString("display_name"))
            : null;
      }
    }
  }

  boolean deleteOwnUser(String token) throws SQLException {
    UserResponse user = findByToken(token);
    if (user == null) {
      return false;
    }
    try (Connection connection = dataSource.getConnection();
        PreparedStatement statement =
            connection.prepareStatement("DELETE FROM app_user WHERE id = ?")) {
      statement.setInt(1, user.id());
      return statement.executeUpdate() == 1;
    }
  }

  private UserRecord findByEmail(String email) throws SQLException {
    try (Connection connection = dataSource.getConnection();
        PreparedStatement statement =
            connection.prepareStatement(
                "SELECT id, email, display_name, password_hash FROM app_user WHERE email = ?")) {
      statement.setString(1, normalizeEmail(email));
      try (ResultSet result = statement.executeQuery()) {
        return result.next()
            ? new UserRecord(
                new UserResponse(
                    result.getInt("id"),
                    result.getString("email"),
                    result.getString("display_name")),
                result.getString("password_hash"))
            : null;
      }
    }
  }

  private String normalizeEmail(String email) {
    return email.trim().toLowerCase(java.util.Locale.ROOT);
  }

  private record UserRecord(UserResponse response, String passwordHash) {}
}
