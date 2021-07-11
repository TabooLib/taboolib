plugins {
    java
    kotlin("jvm") version "1.5.10"
}

repositories {
    maven {
        url = uri("https://repo2s.ptms.ink/repository/maven-releases/")
    }
    mavenCentral()
}

dependencies {
    compileOnly("ink.ptms.core:v11605:11605")
    compileOnly(project(":common"))
    compileOnly(project(":module-nms"))
    compileOnly(kotlin("stdlib"))
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
    }
}