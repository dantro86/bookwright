plugins {
    java
    id("io.qameta.allure") version "2.12.0"
    id("io.freefair.lombok") version "8.13"
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
    implementation("com.squareup.okhttp3:logging-interceptor")
    implementation("com.fasterxml.jackson.core:jackson-databind")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
    implementation("io.qameta.allure:allure-junit5")
    implementation("io.qameta.allure:allure-okhttp3")
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
}

allure {
    version = Versions.ALLURE
    adapter {
        autoconfigure = true
        frameworks {
            junit5 {
                adapterVersion = Versions.ALLURE
            }
        }
    }
}

tasks.test {
    dependsOn("validateVersion")
    useJUnitPlatform {
        val includeTags = System.getProperty("includeTags")
        val excludeTags = System.getProperty("excludeTags")
        if (!includeTags.isNullOrBlank()) includeTags(*includeTags.split(",").toTypedArray())
        if (!excludeTags.isNullOrBlank()) excludeTags(*excludeTags.split(",").toTypedArray())
    }
    // Every STAND/secret knob is passed through to the JVM running the tests
    listOf("STAND", "DB_PASSWORD", "SSH_PASSWORD").forEach { key ->
        (System.getProperty(key) ?: System.getenv(key))?.let { systemProperty(key, it) }
    }
    testLogging {
        events("passed", "failed", "skipped")
        showStandardStreams = System.getProperty("verbose") != null
    }
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

tasks.register("printVersion") {
    group = "help"
    description = "Prints the current bookwright version."
    doLast {
        println(project.version)
    }
}
