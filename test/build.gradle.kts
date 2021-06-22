import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("com.github.johnrengelman.shadow") version "7.0.0"
    id("application")
    java
    kotlin("jvm") version "1.5.10"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":common"))
    implementation(project(":common-5"))
    implementation(project(":module-configuration"))
    compileOnly(kotlin("stdlib"))
}

tasks {
    named<ShadowJar>("shadowJar") {
        archiveClassifier.set("")
        dependencies {
            include(project(":common"))
            include(project(":common-5"))
            include(dependency("dev.vankka.DependencyDownload:common:1.0.0"))
            include(dependency("dev.vankka.DependencyDownload:runtime:1.0.0"))
        }
        relocate("dev.vankka", "taboolib.library")
    }
    build {
        dependsOn(shadowJar)
    }
}

application {
    mainClass.set("taboolib.test.Test")
}