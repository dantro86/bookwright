package io.bookwright.tests.framework;

import static org.assertj.core.api.Assertions.assertThat;

import io.bookwright.junit.NamespaceRegistry;
import io.bookwright.junit.TestStore;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

class StateOwnershipArchitectureTest {

  @Test
  void namespaceRegistryOwnsNoStateKeys() {
    assertThat(NamespaceRegistry.class.getDeclaredFields()).isEmpty();
  }

  @Test
  void testStoreExposesTypedReadsOnly() {
    assertThat(
            Arrays.stream(TestStore.class.getDeclaredMethods())
                .filter(method -> Modifier.isPublic(method.getModifiers()))
                .map(Method::getName))
        .containsExactlyInAnyOrder("authSession", "testUser", "testData", "booking")
        .doesNotContain("get", "put", "getRequired");
  }

  @Test
  void preconditionsDoNotUseExceptionsForExpectedBranching() throws Exception {
    String source =
        Files.readString(Path.of("src/main/java/io/bookwright/junit/Precondition.java"));
    assertThat(source).doesNotContain("catch (");
  }
}
