import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("com.github.johnrengelman.shadow") version "7.0.0"
    java
    kotlin("jvm") version "1.5.10"
}

repositories {
    maven { url = uri("https://repo.codemc.io/repository/maven-public/") }
    mavenCentral()
}

dependencies {
    implementation("net.md-5:bungeecord-chat:1.17-R0.1-SNAPSHOT")//-20210614.231035-9")
    compileOnly(project(":common"))
    compileOnly(kotlin("stdlib"))
}

tasks {
    named<ShadowJar>("shadowJar") {
        archiveClassifier.set("")
        exclude("mojang-translations/*")
        dependencies {
            include(dependency("net.md-5:bungeecord-chat:1.17-R0.1-SNAPSHOT"))//1.17-R0.1-20210614.231035-9"))
        }
        relocate("net.md_5.bungee.chat", "taboolib.library.chat")
        relocate("net.md_5.bungee.api.chat.hover.content", "taboolib.library.chat")
        relocate("net.md_5.bungee.api.chat", "taboolib.library.chat")
        relocate("net.md_5.bungee.api", "taboolib.library.chat")
    }
    build {
        dependsOn(shadowJar)
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
    }
}