plugins {
    java
    kotlin("jvm") version "1.5.10"
}

repositories {
    maven {
        isAllowInsecureProtocol = true
        url = uri("http://repo.ptms.ink/repository/maven-releases/")
    }
    maven { url = uri("https://maven.aliyun.com/repository/central") }
    maven { url = uri("https://repo.codemc.io/repository/maven-public/") }
    maven { url = uri("https://repo.codemc.io/repository/nms/") }
    maven { url = uri("https://oss.sonatype.org/content/repositories/snapshots") }
    maven { url = uri("https://nexus.velocitypowered.com/repository/maven-public/") }
    mavenCentral()
}

dependencies {
    compileOnly("com.google.guava:guava:21.0")
    compileOnly("com.velocitypowered:velocity-api:1.1.8")
    compileOnly("net.md-5:bungeecord-bootstrap:+")/*1.17-R0.1-20210614.231150-8@jar*/
    compileOnly("ink.ptms.core:v11605:11605")
    compileOnly(project(":common"))
    compileOnly(kotlin("stdlib"))
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
    }
}