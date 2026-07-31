package io.bookwright.config;

import org.aeonbits.owner.Config;

/**
 * SSH bastion settings for the database tunnel. Password authentication and disabled host-key
 * checking are accepted only by the explicit local demo profile; non-local stands use a private key
 * and known_hosts.
 */
@Config.LoadPolicy(Config.LoadType.MERGE)
@Config.Sources({"system:properties", "system:env", "classpath:stands/${STAND}/stand.properties"})
public interface SshConfig extends Config {

  enum AuthMode {
    PASSWORD,
    PRIVATE_KEY
  }

  @Key("ssh.host")
  String host();

  @Key("ssh.port")
  @DefaultValue("22")
  int port();

  @Key("ssh.user")
  String user();

  @Key("SSH_PASSWORD")
  @DefaultValue("")
  String password();

  @Key("ssh.auth.mode")
  @DefaultValue("PRIVATE_KEY")
  AuthMode authMode();

  @Key("ssh.private.key.path")
  @DefaultValue("")
  String privateKeyPath();

  @Key("SSH_KEY_PASSPHRASE")
  @DefaultValue("")
  String privateKeyPassphrase();

  @Key("ssh.known.hosts.path")
  @DefaultValue("")
  String knownHostsPath();

  @Key("ssh.strict.host.key.checking")
  @DefaultValue("true")
  boolean strictHostKeyChecking();
}
