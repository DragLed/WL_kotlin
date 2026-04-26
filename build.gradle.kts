plugins {
    kotlin("jvm") version "2.3.10"
    kotlin("plugin.serialization") version "2.3.10"
    application
}

group = "com.wishlistApp"
version = "1.0.0"

repositories {
    mavenCentral()
}

val ktorVersion = "3.4.2"
val exposedVersion = "0.45.0"

dependencies {
    // --- Ktor ---
    implementation("io.ktor:ktor-server-core:$ktorVersion")
    implementation("io.ktor:ktor-server-netty:$ktorVersion")
    implementation("io.ktor:ktor-server-routing:$ktorVersion")
    implementation("io.ktor:ktor-server-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")

    // --- Logging (фикс SLF4J) ---
    implementation("ch.qos.logback:logback-classic:1.4.14")

    // --- Database (Exposed) ---
    implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-dao:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")

    // --- PostgreSQL ---
    implementation("org.postgresql:postgresql:42.7.1")

    // --- Serialization ---
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")

    // --- Tests ---
    testImplementation(kotlin("test"))
}


kotlin {
    jvmToolchain(21) // ⚠️ лучше 21, а не 25 (стабильнее)
}

tasks.test {
    useJUnitPlatform()
}