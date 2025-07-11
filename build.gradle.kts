
plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ktor)
    kotlin("plugin.serialization") version "2.2.0"
}

group = "com.example"
version = "0.0.1"

application {
    mainClass = "io.ktor.server.netty.EngineMain"

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.config.yaml)
    testImplementation(libs.ktor.server.test.host)
    implementation(libs.ktor.server.html.builder)

    implementation(libs.logback.classic)
    testImplementation(libs.kotlin.test.junit)

    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.client.engine.cio)
    implementation(libs.ktor.client.auth)

    implementation(libs.ktor.serialization.kotlinx.json)

    implementation(libs.dotenv.kotlin)
}

ktor {
    fatJar {
        archiveFileName.set("app.jar")
    }
}
