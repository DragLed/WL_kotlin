plugins {
    kotlin("jvm") version "2.3.10"
    kotlin("plugin.serialization") version "2.3.10"
    application
}

application {
    mainClass.set("com.wishlistApp.MainKt")
}

group = "com.wishlistApp"
version = "1.0.0"

repositories {
    mavenCentral()
}

val ktorVersion = "2.3.12"
val exposedVersion = "0.45.0"

dependencies {
    // --- Ktor ---
    implementation("io.ktor:ktor-server-core:$ktorVersion")
    implementation("io.ktor:ktor-server-netty:$ktorVersion")
    implementation("io.ktor:ktor-server-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")

    // --- Logging ---
    implementation("ch.qos.logback:logback-classic:1.4.14")

    // --- Exposed (ORM) ---
    implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-dao:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")

    // --- PostgreSQL ---
    implementation("org.postgresql:postgresql:42.7.1")

    // --- Serialization ---
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")

    // --- Tests ---
    testImplementation(kotlin("test"))

    implementation("io.ktor:ktor-server-swagger:2.3.0")
    implementation("io.ktor:ktor-server-openapi:2.3.0")
}


kotlin {
    jvmToolchain(21) // ⚠️ лучше 21, а не 25 (стабильнее)
}

tasks.test {
    useJUnitPlatform()
}

