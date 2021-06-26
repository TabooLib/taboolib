plugins {
    java
    kotlin("jvm") version "1.5.10"
}

repositories {
    mavenCentral()
    maven { url = uri("https://repo.codemc.io/repository/maven-public/") }
}

dependencies {
    compileOnly("net.md-5:bungeecord-bootstrap:1.17-R0.1-SNAPSHOT")
    compileOnly(project(":common"))
    compileOnly(kotlin("stdlib"))
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
    }
}