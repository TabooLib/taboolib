plugins {
    java
    kotlin("jvm") version "1.5.10"
}

repositories {
    mavenCentral()
}

dependencies {
    compileOnly("com.google.guava:guava:17.0")
    compileOnly("com.google.code.gson:gson:2.3.1")
    compileOnly(kotlin("stdlib"))
}