import java.util.Properties

val ktor_version: String by project
val kotlin_version: String by project
val logback_version: String by project

plugins {
    kotlin("jvm") version "1.9.22"
    id("io.ktor.plugin") version "2.3.8"
    id("com.github.gmazzo.buildconfig") version "5.3.5"
}

group = "ua.pp.leonidius"
version = "0.0.1"

buildConfig {

    val secretFile = file("secret.properties")
    val props = Properties()
    if (secretFile.exists()) {
        secretFile.inputStream().use { props.load(it) }
    } else {
        throw IllegalStateException("Secret file not found")
    }

    buildConfigField("API_KEY", props.getProperty("API_KEY"))
}

application {
    mainClass.set("ua.pp.leonidius.ApplicationKt")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")


}

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.ktor:ktor-server-core-jvm")
    implementation("io.ktor:ktor-server-netty-jvm")
    implementation("ch.qos.logback:logback-classic:$logback_version")
    testImplementation("io.ktor:ktor-server-tests-jvm")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlin_version")
}
