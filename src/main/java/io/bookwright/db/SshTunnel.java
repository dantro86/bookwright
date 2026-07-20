package io.bookwright.db;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import io.bookwright.config.Configs;
import io.bookwright.config.DbConfig;
import io.bookwright.config.SshConfig;
import lombok.extern.slf4j.Slf4j;

/**
 * SSH local port forwarding to MySQL through the bastion:
 * {@code localhost:<db.tunnel.port> -> (ssh) -> <db.host>:<db.port>}.
 * Opened lazily on first DB access, closed by {@link io.bookwright.junit.SshTunnelListener}
 * at the end of the run. Fail-fast: no retry loops, a clear error instead.
 */
@Slf4j
public final class SshTunnel {

    private static Session session;

    private SshTunnel() {
    }

    public static synchronized void ensureOpen() {
        if (session != null && session.isConnected()) {
            return;
        }
        SshConfig ssh = Configs.ssh();
        DbConfig db = Configs.db();
        try {
            Session newSession = new JSch().getSession(ssh.user(), ssh.host(), ssh.port());
            newSession.setPassword(ssh.password());
            newSession.setConfig("StrictHostKeyChecking", "no");
            newSession.setServerAliveInterval(10_000);
            newSession.connect(10_000);
            newSession.setPortForwardingL(db.tunnelPort(), db.host(), db.port());
            session = newSession;
            log.info("SSH tunnel open: localhost:{} -> {}:{} via {}@{}:{}",
                    db.tunnelPort(), db.host(), db.port(), ssh.user(), ssh.host(), ssh.port());
        } catch (JSchException e) {
            throw new IllegalStateException(
                    "Could not open SSH tunnel via %s@%s:%d. Is docker compose up? (docker/docker-compose.yml)"
                            .formatted(ssh.user(), ssh.host(), ssh.port()), e);
        }
    }

    public static synchronized void close() {
        if (session != null) {
            session.disconnect();
            session = null;
            log.info("SSH tunnel closed");
        }
    }
}
