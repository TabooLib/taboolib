plugins {
    java
    kotlin("jvm") version "1.5.10"
}

repositories {
    mavenCentral()
    maven { url = uri("https://oss.sonatype.org/content/repositories/snapshots") }
}

dependencies {
    compileOnly("net.md-5:bungeecord-api:1.17-R0.1-SNAPSHOT")
    implementation(project(":common"))
    implementation(kotlin("stdlib"))
}
