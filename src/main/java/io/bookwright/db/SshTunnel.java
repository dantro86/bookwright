package io.bookwright.db;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import io.bookwright.config.Configs;
import io.bookwright.config.DbConfig;
import io.bookwright.config.InfrastructureConfigValidator;
import io.bookwright.config.SshConfig;
import lombok.extern.slf4j.Slf4j;

/**
 * SSH local port forwarding to MySQL through the bastion: {@code localhost:<db.tunnel.port> ->
 * (ssh) -> <db.host>:<db.port>}. Opened lazily on first DB access, closed by {@link
 * io.bookwright.junit.SshTunnelListener} at the end of the run. Fail-fast: no retry loops, a clear
 * error instead.
 */
@Slf4j
public final class SshTunnel {

  private static Session session;
  private static int localPort;

  private SshTunnel() {}

  public static synchronized int ensureOpen() {
    if (session != null && session.isConnected()) {
      return localPort;
    }
    SshConfig ssh = Configs.ssh();
    DbConfig db = Configs.db();
    InfrastructureConfigValidator.validate(db, ssh, Configs.stand());
    try {
      JSch jsch = configuredJsch(ssh);
      Session newSession = jsch.getSession(ssh.user(), ssh.host(), ssh.port());
      configureAuthentication(newSession, ssh);
      newSession.setConfig("StrictHostKeyChecking", ssh.strictHostKeyChecking() ? "yes" : "no");
      newSession.setServerAliveInterval(10_000);
      newSession.connect(10_000);
      localPort = newSession.setPortForwardingL(db.tunnelPort(), db.host(), db.port());
      session = newSession;
      log.info(
          "SSH tunnel open: localhost:{} -> {}:{} via {}@{}:{}",
          localPort,
          db.host(),
          db.port(),
          ssh.user(),
          ssh.host(),
          ssh.port());
      return localPort;
    } catch (JSchException e) {
      throw new IllegalStateException(
          "Could not open SSH tunnel via %s@%s:%d. Is docker compose up? (docker/docker-compose.yml)"
              .formatted(ssh.user(), ssh.host(), ssh.port()),
          e);
    }
  }

  public static synchronized void close() {
    if (session != null) {
      session.disconnect();
      session = null;
      localPort = 0;
      log.info("SSH tunnel closed");
    }
  }

  private static JSch configuredJsch(SshConfig ssh) throws JSchException {
    JSch jsch = new JSch();
    if (ssh.strictHostKeyChecking()) {
      jsch.setKnownHosts(ssh.knownHostsPath());
    }
    if (ssh.authMode() == SshConfig.AuthMode.PRIVATE_KEY) {
      if (ssh.privateKeyPassphrase().isBlank()) {
        jsch.addIdentity(ssh.privateKeyPath());
      } else {
        jsch.addIdentity(ssh.privateKeyPath(), ssh.privateKeyPassphrase());
      }
    }
    return jsch;
  }

  private static void configureAuthentication(Session target, SshConfig ssh) {
    if (ssh.authMode() == SshConfig.AuthMode.PASSWORD) {
      target.setPassword(ssh.password());
      target.setConfig("PreferredAuthentications", "password");
    } else {
      target.setConfig("PreferredAuthentications", "publickey");
    }
  }
}
