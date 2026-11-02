# CI quality gates

bookwright separates fast framework verification from product-system scenarios. The required GitHub status is `Required quality gate`, which succeeds only when all six inputs pass.

| Gate | Gradle command | Contract |
|---|---|---|
| Static quality | `spotlessCheck validateVersion validateChangelogStyle compileJava compileTestJava` | Formatting, release-note style, version metadata, and compilation |
| Framework | `frameworkJacocoVerification` | Deterministic self-tests and at least 60% instruction coverage of selected framework packages |
| API | `run-local-tests.sh apiTest` | Retrofit scenarios against the digest-pinned local API |
| UI | `uiTest` | Playwright scenarios against Sauce Demo |
| DB over SSH | `run-local-tests.sh dbTest` | JDBI scenarios through the local SSH bastion |
| Integrated application | `run-local-tests.sh integrationTest` | Real booking persistence plus `NEW`/`EXISTING` user fixtures and API-authenticated Playwright UI |

Each test gate uploads an independent Allure result artifact. A successful `main` run merges those artifacts, restores the previous report history, generates one report, and publishes it to GitHub Pages.

Security automation is intentionally separate:

- CodeQL analyzes Java on pushes, pull requests, and a weekly schedule.
- Dependency Review rejects newly introduced dependencies with moderate-or-higher known vulnerabilities.
- Dependabot proposes grouped Gradle and GitHub Actions updates weekly. Dependency and plugin versions live in `gradle/libs.versions.toml` for its benefit: a version hidden behind a Kotlin constant in a build script is invisible to it and silently rots.
- Gradle dependency verification checks committed SHA-256 values before using resolved artifacts.

For branch protection, require `Required quality gate`, `CodeQL`, and `Dependency review`. The last check applies to pull requests only.

## Local commands

```bash
./gradlew spotlessApply        # apply the repository format
./gradlew qualityGate          # deterministic framework quality gate
./gradlew dependencyUpdates    # inspect available dependency upgrades
./scripts/run-local-tests.sh   # complete local stand
```

When intentionally changing dependencies, review the artifacts and regenerate checksums explicitly:

```bash
./gradlew --write-verification-metadata sha256 compileTestJava frameworkTest allureReport
```

Checksum changes are security-sensitive and must not be accepted blindly.

Regeneration records only the artifacts this machine actually downloads. A file already cached in `~/.gradle` leaves no checksum behind, verification still passes locally, and every gate — `Static quality` included — then fails on a clean runner with `<artifact> failed verification`. Confirm the committed file from an empty cache before pushing:

```bash
GRADLE_USER_HOME="$(mktemp -d)" ./gradlew --no-daemon clean compileTestJava frameworkTest
```

Some components publish Gradle Module Metadata next to the POM, and both are fetched. A missing `.module` checksum is copied from Maven Central after its published SHA-1 matches the downloaded file, and is recorded with `origin="Maven Central"` to keep hand-added values distinguishable from generated ones.
