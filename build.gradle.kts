plugins {
    kotlin("jvm") version "1.9.22"
    kotlin("plugin.serialization") version "1.9.22"
    application
}

group = "gokhale.hemant"
version = "1.0-SNAPSHOT"
val ktorVersion = "2.3.11"
val jsonWebTokenVersion = "0.12.5"

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.jsonwebtoken:jjwt-api:$jsonWebTokenVersion")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:$jsonWebTokenVersion")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:$jsonWebTokenVersion")
    implementation("io.ktor:ktor-client-core:$ktorVersion")
    implementation("io.ktor:ktor-client-cio:$ktorVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(17)
}

application {
    mainClass.set("MainKt")
}

