import org.gradle.api.tasks.testing.Test
import org.gradle.testing.jacoco.tasks.JacocoCoverageVerification
import org.gradle.testing.jacoco.tasks.JacocoReport

plugins {
    java
    jacoco
    alias(libs.plugins.allure)
    alias(libs.plugins.lombok)
    alias(libs.plugins.spotless)
    alias(libs.plugins.versions)
}

group = "io.bookwright"
version = providers.gradleProperty("projectVersion").get()

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    constraints {
        // Guice 7.0.0 still brings Guava 31.0.1, which carries deserialization advisories fixed in
        // 33.7.2. Nothing here uses Guava directly, so raise the transitive version instead of
        // declaring a dependency the code does not have.
        implementation(libs.guava) {
            because("Guava below 33.7.2 is affected by advisories against the version Guice resolves")
        }
    }

    implementation(platform(libs.jackson.bom))
    implementation(platform(libs.okhttp.bom))
    implementation(platform(libs.allure.bom))

    implementation(libs.guice)
    implementation(libs.retrofit.core)
    implementation(libs.retrofit.converter.jackson)
    implementation(libs.okhttp.core)
    implementation(libs.jackson.databind)
    implementation(libs.jackson.datatype.jsr310)
    implementation(libs.allure.junit5)
    implementation(libs.allure.assertj)
    implementation(libs.playwright)
    implementation(libs.owner)
    implementation(libs.awaitility)
    implementation(libs.jdbi.core)
    implementation(libs.jdbi.sqlobject)
    implementation(libs.mysql.connector)
    implementation(libs.hikaricp)
    implementation(libs.jsch)
    implementation(libs.assertj.core)
    implementation(libs.logback.classic)

    implementation(platform(libs.junit.bom))
    implementation(libs.junit.jupiter)
    implementation(libs.junit.platform.launcher)

    testImplementation(libs.okhttp.mockwebserver)
}

allure {
    version = libs.versions.allure.cli.get()
    adapter {
        autoconfigure = true
        frameworks {
            junit5 {
                adapterVersion = libs.versions.allure.java.get()
            }
        }
    }
}

spotless {
    java {
        target("src/**/*.java", "local-app/src/**/*.java")
        googleJavaFormat()
        removeUnusedImports()
        trimTrailingWhitespace()
        endWithNewline()
    }
    format("projectFiles") {
        target(
            "*.md",
            "*.kts",
            "*.properties",
            "gradle/*.toml",
            ".github/**/*.yml",
            "docker/**/*.yml",
            "scripts/**/*.sh",
        )
        trimTrailingWhitespace()
        endWithNewline()
    }
}

fun Test.configureBookwrightTestRuntime() {
    dependsOn("validateVersion")
    useJUnitPlatform()
    listOf("STAND", "DB_PASSWORD", "SSH_PASSWORD", "test.seed").forEach { key ->
        (System.getProperty(key) ?: System.getenv(key))?.let { systemProperty(key, it) }
    }
    val configPrefixes = listOf("api.", "ui.", "db.", "ssh.", "teardown.", "local.booking.", "local.user.")
    System.getProperties().stringPropertyNames()
        .filter { key -> configPrefixes.any(key::startsWith) }
        .forEach { key -> systemProperty(key, System.getProperty(key)) }
    testLogging {
        events("passed", "failed", "skipped")
        showStandardStreams = System.getProperty("verbose") != null
    }
}

tasks.withType<Test>().configureEach {
    configureBookwrightTestRuntime()
}

tasks.test {
    useJUnitPlatform {
        val includeTags = System.getProperty("includeTags")
        val excludeTags = System.getProperty("excludeTags")
        if (!includeTags.isNullOrBlank()) includeTags(*includeTags.split(",").toTypedArray())
        if (!excludeTags.isNullOrBlank()) excludeTags(*excludeTags.split(",").toTypedArray())
    }
}

val frameworkTest = tasks.register<Test>("frameworkTest") {
    group = "verification"
    description = "Runs deterministic self-tests for the framework infrastructure."
    filter {
        includeTestsMatching("io.bookwright.api.*")
        includeTestsMatching("io.bookwright.config.*")
        includeTestsMatching("io.bookwright.junit.*")
        includeTestsMatching("io.bookwright.teardown.*")
        includeTestsMatching("io.bookwright.ui.*")
        includeTestsMatching("io.bookwright.util.*")
        includeTestsMatching("io.bookwright.tests.framework.*")
    }
}

tasks.register<Test>("apiTest") {
    group = "verification"
    description = "Runs API product scenarios."
    filter { includeTestsMatching("io.bookwright.tests.api.*") }
}

tasks.register<Test>("uiTest") {
    group = "verification"
    description = "Runs Playwright product scenarios."
    filter { includeTestsMatching("io.bookwright.tests.ui.*") }
}

tasks.register<Test>("dbTest") {
    group = "verification"
    description = "Runs database scenarios through the SSH tunnel."
    filter { includeTestsMatching("io.bookwright.tests.db.*") }
}

tasks.register<Test>("integrationTest") {
    group = "verification"
    description = "Runs cross-layer API-to-database scenarios against the integrated local system."
    filter { includeTestsMatching("io.bookwright.tests.integration.*") }
}

val frameworkCoverageClasses = sourceSets.main.get().output.asFileTree.matching {
    include(
        "io/bookwright/api/**",
        "io/bookwright/config/**",
        "io/bookwright/junit/**",
        "io/bookwright/teardown/**",
        "io/bookwright/ui/**",
        "io/bookwright/util/**",
    )
    exclude("io/bookwright/api/model/**")
}

val frameworkJacocoReport = tasks.register<JacocoReport>("frameworkJacocoReport") {
    group = "verification"
    description = "Generates JaCoCo coverage for framework self-tests."
    dependsOn(frameworkTest)
    executionData(layout.buildDirectory.file("jacoco/frameworkTest.exec"))
    sourceDirectories.setFrom(sourceSets.main.get().allSource.srcDirs)
    classDirectories.setFrom(frameworkCoverageClasses)
    reports {
        xml.required = true
        html.required = true
    }
}

tasks.register<JacocoCoverageVerification>("frameworkJacocoVerification") {
    group = "verification"
    description = "Enforces the minimum framework self-test coverage."
    dependsOn(frameworkJacocoReport)
    executionData(layout.buildDirectory.file("jacoco/frameworkTest.exec"))
    sourceDirectories.setFrom(sourceSets.main.get().allSource.srcDirs)
    classDirectories.setFrom(frameworkCoverageClasses)
    violationRules {
        rule {
            limit { minimum = "0.60".toBigDecimal() }
        }
    }
}

tasks.register("qualityGate") {
    group = "verification"
    description = "Runs deterministic local quality checks without external product systems."
    dependsOn("spotlessCheck", "frameworkJacocoVerification", "validateVersion", "validateChangelogStyle")
}

tasks.register("validateVersion") {
    group = "verification"
    description = "Checks that projectVersion is valid SemVer and matches a release tag when present."

    doLast {
        val projectVersion = project.version.toString()
        val semVer = Regex(
            """^(0|[1-9]\d*)\.(0|[1-9]\d*)\.(0|[1-9]\d*)(?:-((?:0|[1-9]\d*|\d*[A-Za-z-][0-9A-Za-z-]*)(?:\.(?:0|[1-9]\d*|\d*[A-Za-z-][0-9A-Za-z-]*))*))?(?:\+([0-9A-Za-z-]+(?:\.[0-9A-Za-z-]+)*))?$"""
        )
        check(semVer.matches(projectVersion)) {
            "projectVersion '$projectVersion' is not valid Semantic Versioning"
        }

        val releaseTag = System.getenv("GITHUB_REF_NAME")
            ?.takeIf { System.getenv("GITHUB_REF_TYPE") == "tag" }
        if (releaseTag != null) {
            check(releaseTag == "v$projectVersion") {
                "Release tag '$releaseTag' does not match projectVersion '$projectVersion' (expected v$projectVersion)"
            }
        }
    }
}

tasks.register("validateChangelogStyle") {
    group = "verification"
    description = "Rejects changelog bullets that repeat their section heading."

    doLast {
        val repeatedPrefixes = mapOf(
            "Added" to "Added ",
            "Changed" to "Changed ",
            "Deprecated" to "Deprecated ",
            "Removed" to "Removed ",
            "Fixed" to "Fixed ",
            "Security" to "Security ",
        )
        var section: String? = null
        val violations = mutableListOf<String>()
        file("CHANGELOG.md").readLines().forEachIndexed { index, line ->
            if (line.startsWith("### ")) section = line.removePrefix("### ").trim()
            val repeated = repeatedPrefixes[section]
            if (repeated != null && line.startsWith("- $repeated")) {
                violations += "CHANGELOG.md:${index + 1}: '$section' bullet repeats '$repeated'"
            }
        }
        check(violations.isEmpty()) { violations.joinToString(System.lineSeparator()) }
    }
}

tasks.register("printVersion") {
    group = "help"
    description = "Prints the current bookwright version."
    doLast {
        println(project.version)
    }
}
