package io.bookwright.junit;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class UiArtifactsOnFailureExtensionTest {

  @Test
  void oneArtifactFailureDoesNotBlockRemainingCaptures() {
    List<String> captured = new ArrayList<>();

    UiArtifactsOnFailureExtension.captureAll(
        new UiArtifactsOnFailureExtension.Artifact(
            "screenshot",
            () -> {
              captured.add("screenshot");
              throw new IllegalStateException("page already closed");
            }),
        new UiArtifactsOnFailureExtension.Artifact("HTML", () -> captured.add("HTML")),
        new UiArtifactsOnFailureExtension.Artifact(
            "diagnostics", () -> captured.add("diagnostics")),
        new UiArtifactsOnFailureExtension.Artifact("trace", () -> captured.add("trace")));

    assertThat(captured).containsExactly("screenshot", "HTML", "diagnostics", "trace");
  }
}
