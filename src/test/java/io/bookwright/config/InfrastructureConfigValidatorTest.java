package io.bookwright.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import org.aeonbits.owner.ConfigFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InfrastructureConfigValidatorTest {

    @TempDir
    Path tempDirectory;

    @Test
    void acceptsPasswordAuthenticationOnlyForLocalLoopbackDemo() {
        Properties properties = validDatabase();
        properties.putAll(passwordSsh());

        assertThatCode(() -> InfrastructureConfigValidator.validate(
                db(properties), ssh(properties), "local"))
                .doesNotThrowAnyException();
    }

    @Test
    void rejectsPasswordAuthenticationOutsideLocalStand() {
        Properties properties = validDatabase();
        properties.putAll(passwordSsh());

        assertThatThrownBy(() -> InfrastructureConfigValidator.validate(
                db(properties), ssh(properties), "staging"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("ssh.auth.mode=PASSWORD is allowed only for STAND=local");
    }

    @Test
    void acceptsStrictPrivateKeyProfileWithKnownHosts() throws IOException {
        Path privateKey = Files.writeString(tempDirectory.resolve("id_ed25519"), "test private key");
        Path knownHosts = Files.writeString(tempDirectory.resolve("known_hosts"), "example.test ssh-ed25519 key");
        Properties properties = validDatabase();
        properties.putAll(privateKeySsh(privateKey, knownHosts));

        assertThatCode(() -> InfrastructureConfigValidator.validate(
                db(properties), ssh(properties), "staging"))
                .doesNotThrowAnyException();
    }

    @Test
    void rejectsPrivateKeyProfileWithoutReadableTrustFiles() {
        Properties properties = validDatabase();
        properties.putAll(privateKeySsh(
                tempDirectory.resolve("missing-key"), tempDirectory.resolve("missing-known-hosts")));

        assertThatThrownBy(() -> InfrastructureConfigValidator.validate(
                db(properties), ssh(properties), "staging"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining(
                        "ssh.private.key.path is not a readable file",
                        "ssh.known.hosts.path is not a readable file");
    }

    @Test
    void reportsAllMissingDatabaseSettingsBeforeConnecting() {
        Properties properties = passwordSsh();
        properties.setProperty("db.host", "");
        properties.setProperty("db.port", "70000");
        properties.setProperty("db.name", "");
        properties.setProperty("db.user", "");
        properties.setProperty("DB_PASSWORD", "");
        properties.setProperty("db.tunnel.port", "-1");

        assertThatThrownBy(() -> InfrastructureConfigValidator.validate(
                db(properties), ssh(properties), "local"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining(
                        "db.host must not be blank",
                        "db.port must be between 1 and 65535",
                        "db.name must not be blank",
                        "db.user must not be blank",
                        "DB_PASSWORD must not be blank",
                        "db.tunnel.port must be between 0 and 65535");
    }

    private DbConfig db(Properties properties) {
        return ConfigFactory.create(DbConfig.class, properties);
    }

    private SshConfig ssh(Properties properties) {
        return ConfigFactory.create(SshConfig.class, properties);
    }

    private Properties validDatabase() {
        Properties properties = new Properties();
        properties.setProperty("db.host", "mysql.internal");
        properties.setProperty("db.port", "3306");
        properties.setProperty("db.name", "hotel");
        properties.setProperty("db.user", "qa");
        properties.setProperty("DB_PASSWORD", "test-password");
        properties.setProperty("db.tunnel.port", "0");
        return properties;
    }

    private Properties passwordSsh() {
        Properties properties = new Properties();
        properties.setProperty("ssh.host", "127.0.0.1");
        properties.setProperty("ssh.port", "2222");
        properties.setProperty("ssh.user", "tunnel");
        properties.setProperty("SSH_PASSWORD", "test-password");
        properties.setProperty("ssh.auth.mode", "PASSWORD");
        properties.setProperty("ssh.strict.host.key.checking", "false");
        return properties;
    }

    private Properties privateKeySsh(Path privateKey, Path knownHosts) {
        Properties properties = new Properties();
        properties.setProperty("ssh.host", "bastion.example.test");
        properties.setProperty("ssh.port", "22");
        properties.setProperty("ssh.user", "qa-runner");
        properties.setProperty("ssh.auth.mode", "PRIVATE_KEY");
        properties.setProperty("ssh.private.key.path", privateKey.toString());
        properties.setProperty("ssh.known.hosts.path", knownHosts.toString());
        properties.setProperty("ssh.strict.host.key.checking", "true");
        return properties;
    }
}
