# CI quality gates

bookwright separates fast framework verification from product-system scenarios. The required GitHub status is `Required quality gate`, which succeeds only when all six inputs pass.

| Gate | Gradle command | Contract |
|---|---|---|
| Static quality | `spotlessCheck validateVersion compileJava compileTestJava` | Formatting, version metadata, and compilation |
| Framework | `frameworkJacocoVerification` | Deterministic self-tests and at least 60% instruction coverage of selected framework packages |
| API | `run-local-tests.sh apiTest` | Retrofit scenarios against the digest-pinned local API |
| UI | `uiTest` | Playwright scenarios against Sauce Demo |
| DB over SSH | `run-local-tests.sh dbTest` | JDBI scenarios through the local SSH bastion |
| API to DB | `run-local-tests.sh integrationTest` | A real local API persists to MySQL, is verified through the SSH tunnel, and cleans up through the API |

Each test gate uploads an independent Allure result artifact. A successful `main` run merges those artifacts, restores the previous report history, generates one report, and publishes it to GitHub Pages.

Security automation is intentionally separate:

- CodeQL analyzes Java on pushes, pull requests, and a weekly schedule.
- Dependency Review rejects newly introduced dependencies with moderate-or-higher known vulnerabilities.
- Dependabot proposes grouped Gradle and GitHub Actions updates weekly.
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
