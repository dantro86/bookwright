package io.bookwright.config;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/** Validates DB and SSH configuration before any connection or tunnel is opened. */
public final class InfrastructureConfigValidator {

    private InfrastructureConfigValidator() {
    }

    public static void validate(DbConfig db, SshConfig ssh, String stand) {
        List<String> errors = new ArrayList<>();
        required(errors, "db.host", db.host());
        port(errors, "db.port", db.port(), false);
        required(errors, "db.name", db.name());
        required(errors, "db.user", db.user());
        required(errors, "DB_PASSWORD", db.password());
        port(errors, "db.tunnel.port", db.tunnelPort(), true);
        required(errors, "ssh.host", ssh.host());
        port(errors, "ssh.port", ssh.port(), false);
        required(errors, "ssh.user", ssh.user());

        if (ssh.authMode() == SshConfig.AuthMode.PASSWORD) {
            required(errors, "SSH_PASSWORD", ssh.password());
            if (!"local".equalsIgnoreCase(stand)) {
                errors.add("ssh.auth.mode=PASSWORD is allowed only for STAND=local");
            }
            if (ssh.strictHostKeyChecking()) {
                errors.add("The local password profile must set ssh.strict.host.key.checking=false");
            }
            if (!isLoopback(ssh.host())) {
                errors.add("The local password profile requires a loopback ssh.host");
            }
        } else {
            if (!ssh.strictHostKeyChecking()) {
                errors.add("PRIVATE_KEY authentication requires ssh.strict.host.key.checking=true");
            }
            readableFile(errors, "ssh.private.key.path", ssh.privateKeyPath());
            readableFile(errors, "ssh.known.hosts.path", ssh.knownHostsPath());
        }

        if (!errors.isEmpty()) {
            throw new IllegalStateException("Invalid infrastructure configuration:\n - "
                    + String.join("\n - ", errors));
        }
    }

    private static void required(List<String> errors, String key, String value) {
        if (value == null || value.isBlank()) {
            errors.add(key + " must not be blank");
        }
    }

    private static void port(List<String> errors, String key, int value, boolean zeroAllowed) {
        int minimum = zeroAllowed ? 0 : 1;
        if (value < minimum || value > 65_535) {
            errors.add(key + " must be between " + minimum + " and 65535, but was " + value);
        }
    }

    private static void readableFile(List<String> errors, String key, String value) {
        if (value == null || value.isBlank()) {
            errors.add(key + " must point to a readable file");
            return;
        }
        Path path = Path.of(value);
        if (!Files.isRegularFile(path) || !Files.isReadable(path)) {
            errors.add(key + " is not a readable file: " + path.toAbsolutePath());
        }
    }

    private static boolean isLoopback(String host) {
        return "localhost".equalsIgnoreCase(host) || "127.0.0.1".equals(host) || "::1".equals(host);
    }
}
