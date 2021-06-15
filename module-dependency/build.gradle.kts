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
    implementation("me.lucko:jar-relocator:1.4")
    compileOnly("dev.vankka.DependencyDownload:common:1.0.0")
    compileOnly("dev.vankka.DependencyDownload:runtime:1.0.0")
    compileOnly(project(":common"))
    compileOnly(kotlin("stdlib"))
}

tasks {
    named<ShadowJar>("shadowJar") {
        archiveClassifier.set("")
        dependencies {
            include(dependency("me.lucko:jar-relocator:1.4"))
        }
        relocate("me.lucko", "taboolib.library")
    }
    build {
        dependsOn(shadowJar)
    }
}