plugins {
    java
    kotlin("jvm") version "1.5.10"
}

repositories {
    mavenCentral()
    maven {
        url = uri("https://nexus.lucko.me/repository/maven-central/")
    }
    maven {
        url = uri("https://repo.ptms.ink/repository/maven-releases/")
    }
}

dependencies {
    api("org.ow2.asm:asm:9.1")
    api("org.ow2.asm:asm-commons:9.1")
    implementation(kotlin("stdlib"))
}
