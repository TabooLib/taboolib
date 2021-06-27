plugins {
    java
    kotlin("jvm") version "1.5.10"
}

repositories {
    mavenCentral()
    maven { url = uri("https://repo.spongepowered.org/maven") }
}

dependencies {
    compileOnly("org.spongepowered:spongeapi:7.2.0")
    compileOnly(project(":common"))
    compileOnly(kotlin("stdlib"))
}
