plugins {
    java
    kotlin("jvm") version "1.5.10"
}

repositories {
    maven {
        url = uri("https://repo1.maven.org/maven2")
    }
    mavenCentral()
}

dependencies {
    compileOnly("org.ow2.asm:asm:9.1")
    compileOnly("org.ow2.asm:asm-commons:9.1")
    compileOnly("com.google.guava:guava:21.0")
    compileOnly("org.apache.commons:commons-lang3:3.5")
    compileOnly(project(":common"))
    compileOnly(project(":module-dependency"))
    compileOnly(kotlin("stdlib"))
}