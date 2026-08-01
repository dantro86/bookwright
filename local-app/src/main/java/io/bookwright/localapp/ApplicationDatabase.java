package io.bookwright.localapp;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

final class ApplicationDatabase implements AutoCloseable {

  private final HikariDataSource dataSource;

  ApplicationDatabase(DatabaseConfig config) {
    HikariConfig hikari = new HikariConfig();
    hikari.setJdbcUrl(
        "jdbc:mysql://%s:%d/%s".formatted(config.host(), config.port(), config.name()));
    hikari.setUsername(config.user());
    hikari.setPassword(config.password());
    hikari.setMaximumPoolSize(4);
    hikari.setConnectionTimeout(10_000);
    dataSource = new HikariDataSource(hikari);
  }

  HikariDataSource dataSource() {
    return dataSource;
  }

  @Override
  public void close() {
    dataSource.close();
  }
}
