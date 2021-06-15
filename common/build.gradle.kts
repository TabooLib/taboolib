import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("com.github.johnrengelman.shadow") version "7.0.0"
    java
    kotlin("jvm") version "1.5.10"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("dev.vankka.DependencyDownload:common:1.0.0")
    implementation("dev.vankka.DependencyDownload:runtime:1.0.0")
    compileOnly(kotlin("stdlib"))
}

tasks {
    named<ShadowJar>("shadowJar") {
        archiveClassifier.set("")
        dependencies {
            include(dependency("dev.vankka.DependencyDownload:common:1.0.0"))
            include(dependency("dev.vankka.DependencyDownload:runtime:1.0.0"))
        }
        relocate("dev.vankka", "taboolib.library")
    }
    build {
        dependsOn(shadowJar)
    }
}