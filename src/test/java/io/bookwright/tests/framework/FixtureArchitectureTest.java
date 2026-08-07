package io.bookwright.tests.framework;

import static org.assertj.core.api.Assertions.assertThat;

import io.bookwright.config.Configs;
import io.bookwright.fixtures.local.LocalUserFixtures;
import io.bookwright.fixtures.saucedemo.SauceDemoFixtures;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

class FixtureArchitectureTest {

  private static final List<String> SCENARIO_LITERALS =
      List.of(
          "locked_out_user",
          "definitely-wrong",
          "incorrect-password",
          "existing.user@bookwright.dev",
          "Sauce Labs Backpack",
          "Sauce Labs Bike Light",
          "Sauce Labs Bolt T-Shirt",
          "Sauce Labs Fleece Jacket",
          "Sauce Labs Onesie",
          "Test.allTheThings() T-Shirt (Red)",
          "\"Products\"",
          "\"Remove\"",
          "Thank you for your order!",
          "Your order has been dispatched",
          ".hasText(\"Bookings\")",
          "Welcome, %s",
          "\"Authentication required\"",
          "Session is missing, invalid, or expired",
          "\"za\"",
          "\"Test\"",
          "\"Guest\"",
          "\"00100\"",
          "\"Wilson\"",
          "\"double\"",
          "\"Tunnel\"",
          "\"Tester\"",
          "999_999_999");

  @Test
  void stepsAndProductTestsDoNotOwnScenarioFixtures() throws Exception {
    List<String> violations = new ArrayList<>();
    inspect(Path.of("src/main/java/io/bookwright/steps"), violations);
    inspect(Path.of("src/test/java/io/bookwright/tests/api"), violations);
    inspect(Path.of("src/test/java/io/bookwright/tests/ui"), violations);
    inspect(Path.of("src/test/java/io/bookwright/tests/integration"), violations);
    inspect(Path.of("src/test/java/io/bookwright/tests/db"), violations);

    assertThat(violations).as("scenario literals outside typed fixtures/TestData").isEmpty();
  }

  @Test
  void fixtureDiagnosticsRedactPasswords() {
    SauceDemoFixtures sauceDemo = SauceDemoFixtures.from(Configs.main());
    LocalUserFixtures local = LocalUserFixtures.from(Configs.main());

    assertThat(sauceDemo.toString())
        .doesNotContain(sauceDemo.standardUser().password())
        .doesNotContain(sauceDemo.invalidPassword().user().password())
        .contains("[REDACTED]");
    assertThat(local.toString())
        .doesNotContain(local.invalidExistingUser().password())
        .contains("[REDACTED]");
  }

  private void inspect(Path root, List<String> violations) throws IOException {
    try (Stream<Path> files = Files.walk(root)) {
      for (Path source : files.filter(path -> path.toString().endsWith(".java")).toList()) {
        String content = Files.readString(source);
        SCENARIO_LITERALS.stream()
            .filter(content::contains)
            .map(literal -> source + " contains " + literal)
            .forEach(violations::add);
      }
    }
  }
}
