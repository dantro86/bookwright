package io.bookwright.config;

import org.aeonbits.owner.Config;

/**
 * MySQL connection settings. The database is only reachable through the SSH tunnel,
 * so tests always connect to {@code localhost:db.tunnel.port}.
 * The password comes from the {@code DB_PASSWORD} env var (local demo default in the stand file).
 */
@Config.LoadPolicy(Config.LoadType.MERGE)
@Config.Sources({
        "system:properties",
        "system:env",
        "classpath:stands/${STAND}/stand.properties"
})
public interface DbConfig extends Config {

    @Key("db.host")
    String host();

    @Key("db.port")
    @DefaultValue("3306")
    int port();

    @Key("db.name")
    String name();

    @Key("db.user")
    String user();

    @Key("DB_PASSWORD")
    String password();

    @Key("db.tunnel.port")
    @DefaultValue("13306")
    int tunnelPort();
}
