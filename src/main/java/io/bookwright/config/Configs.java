package io.bookwright.config;

import org.aeonbits.owner.ConfigCache;
import org.aeonbits.owner.ConfigFactory;

/**
 * Single entry point for configuration. Resolves the target stand once
 * (system property {@code STAND}, then env var, default {@code prod})
 * and hands out cached Owner config instances.
 */
public final class Configs {

    public static final String DEFAULT_STAND = "prod";

    static {
        ConfigFactory.setProperty("STAND", stand());
    }

    private Configs() {
    }

    public static String stand() {
        String stand = System.getProperty("STAND", System.getenv("STAND"));
        return stand == null || stand.isBlank() ? DEFAULT_STAND : stand;
    }

    public static MainConfig main() {
        return ConfigCache.getOrCreate(MainConfig.class);
    }

    public static DbConfig db() {
        return ConfigCache.getOrCreate(DbConfig.class);
    }

    public static SshConfig ssh() {
        return ConfigCache.getOrCreate(SshConfig.class);
    }
}
