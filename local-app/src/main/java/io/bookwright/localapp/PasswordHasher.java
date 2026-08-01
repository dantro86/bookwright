package io.bookwright.localapp;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

final class PasswordHasher {

  private static final int ITERATIONS = 120_000;
  private static final int KEY_LENGTH = 256;
  private static final SecureRandom RANDOM = new SecureRandom();

  private PasswordHasher() {}

  static String hashPassword(String password) {
    byte[] salt = new byte[16];
    RANDOM.nextBytes(salt);
    return "%d:%s:%s"
        .formatted(
            ITERATIONS,
            Base64.getEncoder().encodeToString(salt),
            Base64.getEncoder().encodeToString(derive(password, salt, ITERATIONS)));
  }

  static boolean matches(String password, String encoded) {
    String[] parts = encoded.split(":", 3);
    if (parts.length != 3) {
      return false;
    }
    int iterations = Integer.parseInt(parts[0]);
    byte[] salt = Base64.getDecoder().decode(parts[1]);
    byte[] expected = Base64.getDecoder().decode(parts[2]);
    return MessageDigest.isEqual(expected, derive(password, salt, iterations));
  }

  static String tokenHash(String token) {
    try {
      return java.util.HexFormat.of()
          .formatHex(
              MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8)));
    } catch (NoSuchAlgorithmException exception) {
      throw new IllegalStateException("SHA-256 is not available", exception);
    }
  }

  private static byte[] derive(String password, byte[] salt, int iterations) {
    PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, iterations, KEY_LENGTH);
    try {
      return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
    } catch (Exception exception) {
      throw new IllegalStateException("Could not hash password", exception);
    } finally {
      spec.clearPassword();
    }
  }
}
