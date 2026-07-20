plugins {
    java
    id("io.qameta.allure") version "2.12.0"
    id("io.freefair.lombok") version "8.13"
}

group = "io.bookwright"
version = "1.0-SNAPSHOT"

object Versions {
    const val JUNIT = "5.13.4"
    const val GUICE = "7.0.0"
    const val RETROFIT = "3.0.0"
    const val OKHTTP = "5.1.0"
    const val JACKSON = "2.19.2"
    const val ALLURE = "2.29.1"
    const val PLAYWRIGHT = "1.53.0"
    const val OWNER = "1.0.12"
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
