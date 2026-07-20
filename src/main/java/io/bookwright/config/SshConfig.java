package io.bookwright.config;

import org.aeonbits.owner.Config;

/**
 * SSH bastion settings for the database tunnel.
 * The password comes from the {@code SSH_PASSWORD} env var (local demo default in the stand file).
 */
@Config.LoadPolicy(Config.LoadType.MERGE)
@Config.Sources({
        "system:properties",
        "system:env",
        "classpath:stands/${STAND}/stand.properties"
})
public interface SshConfig extends Config {

    @Key("ssh.host")
    String host();

    @Key("ssh.port")
    @DefaultValue("22")
    int port();

    @Key("ssh.user")
    String user();

    @Key("SSH_PASSWORD")
    String password();
}
