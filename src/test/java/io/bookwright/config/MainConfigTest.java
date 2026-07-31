package io.bookwright.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.parallel.Resources.SYSTEM_PROPERTIES;

import java.util.Arrays;
import org.aeonbits.owner.Config;
import org.aeonbits.owner.ConfigFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceLock;

class MainConfigTest {

  @Test
  void declaresExpectedSourcePrecedence() {
    Config.Sources sources = MainConfig.class.getAnnotation(Config.Sources.class);

    assertThat(Arrays.asList(sources.value()))
        .containsExactly(
            "system:properties", "system:env", "classpath:stands/${STAND}/stand.properties");
    assertThat(MainConfig.class.getAnnotation(Config.LoadPolicy.class).value())
        .isEqualTo(Config.LoadType.MERGE);
  }

  @Test
  @ResourceLock(SYSTEM_PROPERTIES)
  void systemPropertyOverridesStandConfiguration() {
    String key = "api.base.url";
    String previous = System.getProperty(key);
    try {
      System.setProperty(key, "https://system-property.example.test");

      MainConfig config = ConfigFactory.create(MainConfig.class);

      assertThat(config.apiBaseUrl()).isEqualTo("https://system-property.example.test");
    } finally {
      restore(key, previous);
    }
  }

  @Test
  void declaresDocumentedDefaults() throws NoSuchMethodException {
    assertThat(
            MainConfig.class
                .getMethod("uiHeadless")
                .getAnnotation(Config.DefaultValue.class)
                .value())
        .isEqualTo("true");
    assertThat(
            MainConfig.class
                .getMethod("teardownFailOnError")
                .getAnnotation(Config.DefaultValue.class)
                .value())
        .isEqualTo("true");
  }

  private void restore(String key, String previous) {
    if (previous == null) {
      System.clearProperty(key);
    } else {
      System.setProperty(key, previous);
    }
  }
}
