plugins {
    java
    kotlin("jvm") version "1.5.10"
}

repositories {
    mavenCentral()
    maven { url = uri("https://repo.nukkitx.com/maven-snapshots") }
}

dependencies {
    compileOnly("cn.nukkit:nukkit:2.0.0-SNAPSHOT")
    compileOnly(project(":common"))
    compileOnly(kotlin("stdlib"))
}
