package io.bookwright.db;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.bookwright.config.Configs;
import io.bookwright.config.DbConfig;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;

/**
 * Lazy singleton Hikari pool + Jdbi, connecting to MySQL through the SSH tunnel.
 * Hikari re-validates connections, so no custom retry machinery is needed.
 */
public final class DbPool {

    private static volatile Jdbi jdbi;
    private static HikariDataSource dataSource;

    private DbPool() {
    }

    public static Jdbi jdbi() {
        if (jdbi == null) {
            synchronized (DbPool.class) {
                if (jdbi == null) {
                    SshTunnel.ensureOpen();
                    DbConfig db = Configs.db();
                    HikariConfig config = new HikariConfig();
                    config.setJdbcUrl("jdbc:mysql://localhost:%d/%s".formatted(db.tunnelPort(), db.name()));
                    config.setUsername(db.user());
                    config.setPassword(db.password());
                    config.setMaximumPoolSize(4);
                    dataSource = new HikariDataSource(config);
                    jdbi = Jdbi.create(dataSource).installPlugin(new SqlObjectPlugin());
                }
            }
        }
        return jdbi;
    }

    public static synchronized void shutdown() {
        if (dataSource != null) {
            dataSource.close();
            dataSource = null;
            jdbi = null;
        }
    }
}
