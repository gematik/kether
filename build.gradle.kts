plugins {
    kotlin("jvm") version "1.7.10"
    kotlin("plugin.serialization") version "1.7.10"
    id("de.gematik.kether.codegen") version "1.0-SNAPSHOT"
}

group = "de.gematik.kether"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.7.10")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-core-jvm:1.4.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.4.0")
    implementation("com.squareup.okhttp3:okhttp:4.10.0")
    implementation("org.bouncycastle:bcprov-jdk18on:1.71")
    implementation("org.bouncycastle:bcprov-ext-jdk18on:1.71")
    implementation("junit:junit:4.13.1")
    implementation("io.github.microutils:kotlin-logging-jvm:2.0.11")
    implementation("ch.qos.logback:logback-classic:1.2.11")
}

configure<de.gematik.kether.codegen.CodeGeneratorPluginExtension> {
    packageName.set("de.gematik.kether.contracts")
}

