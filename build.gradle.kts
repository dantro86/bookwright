import org.gradle.api.tasks.testing.Test
import org.gradle.testing.jacoco.tasks.JacocoCoverageVerification
import org.gradle.testing.jacoco.tasks.JacocoReport

plugins {
    java
    jacoco
    id("io.qameta.allure") version "2.12.0"
    id("io.freefair.lombok") version "8.13"
    id("com.diffplug.spotless") version "8.8.0"
    id("com.github.ben-manes.versions") version "0.54.0"
}

group = "io.bookwright"
version = providers.gradleProperty("projectVersion").get()

object Versions {
    const val JUNIT = "5.13.4"
    const val GUICE = "7.0.0"
    const val RETROFIT = "3.0.0"
    const val OKHTTP = "5.1.0"
    const val JACKSON = "2.19.2"
    const val ALLURE = "2.29.1"
    const val ALLURE_CLI = "2.39.0"
    const val PLAYWRIGHT = "1.53.0"
    const val OWNER = "1.0.12"
    const val AWAITILITY = "4.3.0"
    const val JDBI = "3.49.5"
    const val MYSQL = "9.3.0"
    const val HIKARI = "6.3.0"
    const val JSCH = "2.27.0"
    const val ASSERTJ = "3.27.3"
    const val LOGBACK = "1.5.18"
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(platform("com.fasterxml.jackson:jackson-bom:${Versions.JACKSON}"))
    implementation(platform("com.squareup.okhttp3:okhttp-bom:${Versions.OKHTTP}"))
    implementation(platform("io.qameta.allure:allure-bom:${Versions.ALLURE}"))

    implementation("com.google.inject:guice:${Versions.GUICE}")
    implementation("com.squareup.retrofit2:retrofit:${Versions.RETROFIT}")
    implementation("com.squareup.retrofit2:converter-jackson:${Versions.RETROFIT}")
    implementation("com.squareup.okhttp3:okhttp")
    implementation("com.fasterxml.jackson.core:jackson-databind")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
    implementation("io.qameta.allure:allure-junit5")
    implementation("io.qameta.allure:allure-assertj")
    implementation("com.microsoft.playwright:playwright:${Versions.PLAYWRIGHT}")
    implementation("org.aeonbits.owner:owner:${Versions.OWNER}")
    implementation("org.awaitility:awaitility:${Versions.AWAITILITY}")
    implementation("org.jdbi:jdbi3-core:${Versions.JDBI}")
    implementation("org.jdbi:jdbi3-sqlobject:${Versions.JDBI}")
    implementation("com.mysql:mysql-connector-j:${Versions.MYSQL}")
    implementation("com.zaxxer:HikariCP:${Versions.HIKARI}")
    implementation("com.github.mwiede:jsch:${Versions.JSCH}")
    implementation("org.assertj:assertj-core:${Versions.ASSERTJ}")
    implementation("ch.qos.logback:logback-classic:${Versions.LOGBACK}")

    implementation(platform("org.junit:junit-bom:${Versions.JUNIT}"))
    implementation("org.junit.jupiter:junit-jupiter")
    implementation("org.junit.platform:junit-platform-launcher")

    testImplementation("com.squareup.okhttp3:mockwebserver")
}

allure {
    version = Versions.ALLURE_CLI
    adapter {
        autoconfigure = true
        frameworks {
            junit5 {
                adapterVersion = Versions.ALLURE
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
        target("*.md", "*.kts", "*.properties", ".github/**/*.yml", "docker/**/*.yml", "scripts/**/*.sh")
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
