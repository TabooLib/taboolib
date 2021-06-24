plugins {
    java
    kotlin("jvm") version "1.5.10"
}

repositories {
    mavenCentral()
}

dependencies {
    compileOnly("org.yaml:snakeyaml:1.28")
    compileOnly(project(":common"))
    compileOnly(project(":common-5"))
    compileOnly(project(":module-chat"))
    compileOnly(kotlin("stdlib"))
}