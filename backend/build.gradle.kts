plugins {
    kotlin("jvm") version "2.4.21"
    kotlin("plugin.serialization") version "2.4.21"
    application
}

group = "no.kartverket.workshop"
version = "0.1.0"

val ktorVersion = "3.6.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.ktor:ktor-server-core:$ktorVersion")
    implementation("io.ktor:ktor-server-netty:$ktorVersion")
    implementation("io.ktor:ktor-server-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-server-status-pages:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
    implementation("org.xerial:sqlite-jdbc:3.53.4.0")
    implementation("com.github.seratch:kotliquery:1.9.1")
    implementation("org.flywaydb:flyway-core:13.10.0")
    implementation("ch.qos.logback:logback-classic:1.6.5")

    testImplementation(kotlin("test"))
    testImplementation("io.ktor:ktor-server-test-host:$ktorVersion")
    testImplementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
}

kotlin {
    jvmToolchain(21)
}

application {
    mainClass.set("no.kartverket.workshop.ApplicationKt")
}

tasks.test {
    useJUnitPlatform()
}

// Fills the local dev database with fictional data. Lives in the test sources so it never ships.
tasks.register<JavaExec>("seed") {
    group = "application"
    description = "Seeds the local SQLite database with fictional dev data"
    classpath = sourceSets.test.get().runtimeClasspath
    mainClass.set("no.kartverket.workshop.DevDataKt")
    environment("DB_URL", System.getenv("DB_URL") ?: "jdbc:sqlite:mini-matrikkel.db")
}
