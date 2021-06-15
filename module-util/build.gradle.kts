plugins {
    java
    kotlin("jvm") version "1.5.10"
}

repositories {
    mavenCentral()
}

dependencies {
    compileOnly(kotlin("stdlib"))
}