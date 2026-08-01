package io.bookwright.localapp;

record DatabaseConfig(
    String host,
    int port,
    String name,
    String user,
    String password,
    String existingUserEmail,
    String existingUserPassword,
    String existingUserName) {

  static DatabaseConfig fromEnvironment() {
    return new DatabaseConfig(
        environment("DB_HOST", "mysql"),
        Integer.parseInt(environment("DB_PORT", "3306")),
        environment("DB_NAME", "hotel"),
        environment("DB_USER", "qa"),
        requiredEnvironment("DB_PASSWORD"),
        environment("EXISTING_USER_EMAIL", "existing.user@bookwright.dev"),
        environment("EXISTING_USER_PASSWORD", "existing_demo_password"),
        environment("EXISTING_USER_NAME", "Existing User"));
  }

  private static String environment(String key, String defaultValue) {
    return System.getenv().getOrDefault(key, defaultValue);
  }

  private static String requiredEnvironment(String key) {
    String value = System.getenv(key);
    if (value == null || value.isBlank()) {
      throw new IllegalStateException("Required environment variable is missing: " + key);
    }
    return value;
  }
}
