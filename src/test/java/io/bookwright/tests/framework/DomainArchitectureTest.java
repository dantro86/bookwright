package io.bookwright.tests.framework;

import static org.assertj.core.api.Assertions.assertThat;

import io.bookwright.api.local.users.UsersApi;
import io.bookwright.api.restfulbooker.health.HealthApi;
import io.bookwright.steps.ApiSteps;
import io.bookwright.steps.UiSteps;
import io.bookwright.steps.local.LocalApiSteps;
import io.bookwright.steps.restfulbooker.RestfulBookerSteps;
import io.bookwright.steps.ui.local.LocalUiSteps;
import io.bookwright.steps.ui.saucedemo.SauceDemoSteps;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

class DomainArchitectureTest {

  @Test
  void retrofitClientsBelongToTargetAndDomainPackages() throws Exception {
    Path apiRoot = Path.of("src/main/java/io/bookwright/api");
    try (Stream<Path> files = Files.walk(apiRoot)) {
      assertThat(
              files
                  .filter(path -> path.getFileName().toString().endsWith("Api.java"))
                  .map(apiRoot::relativize)
                  .map(Path::getNameCount))
          .allMatch(depth -> depth >= 3);
    }
  }

  @Test
  void topLevelFacadesExposeTargetsOnly() {
    assertThat(fieldTypes(ApiSteps.class))
        .containsExactlyInAnyOrder(RestfulBookerSteps.class, LocalApiSteps.class);
    assertThat(fieldTypes(UiSteps.class))
        .containsExactlyInAnyOrder(SauceDemoSteps.class, LocalUiSteps.class);
  }

  @Test
  void splitClientsOwnOnlyTheirDomainOperations() {
    assertThat(methodNames(HealthApi.class)).containsExactly("ping");
    assertThat(methodNames(UsersApi.class))
        .containsExactlyInAnyOrder("register", "deleteCurrentUser");
    assertThat(methodNames(io.bookwright.api.local.auth.AuthApi.class)).containsExactly("login");
  }

  @Test
  void focusedStepsDoNotDependOnSiblingDomainSteps() throws Exception {
    Path stepsRoot = Path.of("src/main/java/io/bookwright/steps");
    List<String> violations;
    try (Stream<Path> files = Files.walk(stepsRoot)) {
      violations =
          files
              .filter(path -> path.toString().endsWith("Steps.java"))
              .filter(
                  path ->
                      Set.of(
                              "AuthSteps.java",
                              "HealthSteps.java",
                              "BookingSteps.java",
                              "UserSteps.java",
                              "LoginSteps.java",
                              "InventorySteps.java",
                              "CheckoutSteps.java")
                          .contains(path.getFileName().toString()))
              .filter(this::importsSiblingDomainStep)
              .map(stepsRoot::relativize)
              .map(Path::toString)
              .toList();
    }
    assertThat(violations).as("domain steps importing sibling domain steps").isEmpty();
  }

  private Set<Class<?>> fieldTypes(Class<?> facade) {
    return Stream.of(facade.getDeclaredFields())
        .map(Field::getType)
        .collect(java.util.stream.Collectors.toSet());
  }

  private Set<String> methodNames(Class<?> client) {
    return Stream.of(client.getDeclaredMethods())
        .map(java.lang.reflect.Method::getName)
        .collect(java.util.stream.Collectors.toSet());
  }

  private boolean importsSiblingDomainStep(Path source) {
    try {
      Path relative = Path.of("src/main/java/io/bookwright/steps").relativize(source);
      String ownPackage =
          "io.bookwright.steps."
              + relative.subpath(0, relative.getNameCount() - 1).toString().replace('/', '.');
      return Files.readAllLines(source).stream()
          .filter(line -> line.startsWith("import io.bookwright.steps."))
          .map(line -> line.substring("import ".length(), line.length() - 1))
          .anyMatch(imported -> !imported.startsWith(ownPackage + "."));
    } catch (java.io.IOException error) {
      throw new IllegalStateException("Cannot inspect " + source, error);
    }
  }
}
