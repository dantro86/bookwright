plugins {
    application
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(platform(libs.jackson.bom))
    implementation(libs.jackson.databind)
    implementation(libs.jackson.datatype.jsr310)
    implementation(libs.hikaricp)
    implementation(libs.mysql.connector)
    implementation(libs.slf4j.api)
    runtimeOnly(libs.logback.classic)
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

application {
    mainClass = "io.bookwright.localapp.BookingApplication"
}
