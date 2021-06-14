plugins {
    java
    kotlin("jvm") version "1.5.10"
}

repositories {
    mavenCentral()
}

dependencies {
    api("com.google.guava:guava:17.0")
    api("com.google.code.gson:gson:2.3.1")
    implementation(kotlin("stdlib"))
}