plugins {
    java
    kotlin("jvm") version "1.5.10"
}

repositories {
    mavenCentral()
}

dependencies {
    compileOnly("org.ow2.asm:asm:9.1")
    compileOnly("org.ow2.asm:asm-commons:9.1")
    compileOnly("com.google.guava:guava:17.0")
    compileOnly(project(":common"))
    compileOnly(project(":module-dependency"))
    compileOnly(kotlin("stdlib"))
}