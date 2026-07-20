package io.bookwright.config;

import org.aeonbits.owner.Config;

/**
 * Merged configuration: system properties win over environment variables,
 * which win over the per-stand properties file selected via {@code -DSTAND=<name>}.
 */
@Config.LoadPolicy(Config.LoadType.MERGE)
@Config.Sources({
        "system:properties",
        "system:env",
        "classpath:stands/${STAND}/stand.properties"
})
public interface MainConfig extends Config {

    @Key("api.base.url")
    String apiBaseUrl();

    @Key("api.username")
    String apiUsername();

    @Key("api.password")
    String apiPassword();

    @Key("ui.base.url")
    String uiBaseUrl();

    @Key("ui.user")
    String uiUser();

    @Key("ui.password")
    String uiPassword();

    @Key("ui.headless")
    @DefaultValue("true")
    boolean uiHeadless();
}
