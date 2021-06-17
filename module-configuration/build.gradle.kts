plugins {
    java
    kotlin("jvm") version "1.5.10"
}

repositories {
    mavenCentral()
}

dependencies {
    compileOnly("org.yaml:snakeyaml:1.28")
    compileOnly(project(":module-chat"))
    compileOnly(project(":module-dependency"))
    compileOnly(kotlin("stdlib"))
}