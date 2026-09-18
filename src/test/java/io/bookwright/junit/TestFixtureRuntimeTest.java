package io.bookwright.junit;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.inject.Inject;
import io.bookwright.config.MainConfig;
import io.bookwright.fixtures.database.HotelDatabaseFixtures;
import io.bookwright.fixtures.local.LocalUserFixtures;
import io.bookwright.fixtures.saucedemo.SauceDemoFixtures;
import io.bookwright.steps.ApiSteps;
import io.bookwright.teardown.TeardownStorage;
import io.bookwright.util.TestData;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class TestFixtureRuntimeTest {

  @Test
  void resolvesAnUnregisteredFixtureFromItsConstructor(
      ApiSteps api, TestData testData, @TestFixture RuntimeProbe fixture) {
    assertThat(api).isNotNull();
    assertThat(fixture.config()).isNotNull();
    assertThat(fixture.testData()).isSameAs(testData);
    assertThat(fixture.teardown()).isNotNull();
  }

  @Test
  void builtInFixturesUseTheSameExplicitContract(
      @TestFixture SauceDemoFixtures sauceDemo,
      @TestFixture LocalUserFixtures localUsers,
      @TestFixture HotelDatabaseFixtures database) {
    assertThat(sauceDemo.standardUser().username()).isNotBlank();
    assertThat(localUsers.ui().authenticatedTitle()).isEqualTo("Bookings");
    assertThat(database.minimumBookingCount()).isPositive();
  }

  @Test
  void stepsResolverDoesNotOwnFixtureTypesOrFactories() throws IOException {
    String source =
        Files.readString(Path.of("src/main/java/io/bookwright/junit/StepsParameterResolver.java"));

    assertThat(source)
        .doesNotContain("io.bookwright.fixtures")
        .doesNotContain("FixtureCatalog")
        .doesNotContain("Configs.main()")
        .doesNotContain("TestDataExtension.getOrCreate");
  }

  static final class RuntimeProbe {

    private final MainConfig config;
    private final TestData testData;
    private final TeardownStorage teardown;

    @Inject
    RuntimeProbe(MainConfig config, TestData testData, TeardownStorage teardown) {
      this.config = config;
      this.testData = testData;
      this.teardown = teardown;
    }

    MainConfig config() {
      return config;
    }

    TestData testData() {
      return testData;
    }

    TeardownStorage teardown() {
      return teardown;
    }
  }
}
