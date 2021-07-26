import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("com.github.johnrengelman.shadow") version "7.0.0"
    java
    kotlin("jvm") version "1.5.10"
}

repositories {
    maven {
        isAllowInsecureProtocol = true
        url = uri("http://repo.ptms.ink/repository/maven-releases/")
    }
    maven {
        url = uri("https://repo.codemc.io/repository/nms/")
    }
    mavenCentral()
}

dependencies {
    compileOnly("ink.ptms.core:v11200:11200:all")
    compileOnly("org.ow2.asm:asm:9.1")
    compileOnly("org.ow2.asm:asm-commons:9.1")
    compileOnly(project(":common"))
    compileOnly(project(":platform-bukkit"))
    compileOnly(kotlin("stdlib"))
}

tasks {
    named<ShadowJar>("shadowJar") {
        archiveClassifier.set("")
        relocate("me.lucko", "taboolib.library")
        relocate("org.objectweb", "taboolib.library")
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